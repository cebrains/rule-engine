package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ConstantFields;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor.DataCheckNodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.SpringContextUtil;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice.JsInvokeService;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice.exception.FunctionInvokeException;
import com.uama.microservices.provider.ruleengine.actorcluster.exception.NodeProcessorException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.CollectionUtils;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.util.*;
import java.util.stream.Collectors;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.ERROR_STR;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 10:29
 **/
public class DefaultDataCheckNodeProcessor extends BaseNodeProcessor<DataCheckNodeActor> {
    
    private JsInvokeService jsInvokeService = SpringContextUtil.getBean(JsInvokeService.class);
    private int jsEvalErrorLogMaxError = 3;
    private enum DataCheckResult {
        SKIP, TRUE, FALSE
    }
    
    public DefaultDataCheckNodeProcessor(DataCheckNodeActor actor) {
        super(actor);
    }
    
    public void processRootRouterUpdateMessage(RootRouterUpdateMessage message) {
        if (!super.checkIfNull(message)) {
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                super.sendToNextNodeHolderActor(super.actor.getNodeIdV(), message, toRelationList, super.actor.getChainIdMap(), super.actor.getActorHolderMap(), false);
            }
        }
    }
    
    public void processNodeDeleteMessage(NodeDeleteMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            // 如果删除时间标记已被标记 并且等于本次的删除标记 则认为删除message已经下发到下层node 不需要再次下发
            if (message.getDeleteTimeStamp() != super.actor.getDeleteTimeStamp() && CollectionUtil.isNotEmpty(toRelationList)) {
                super.actor.setDeleteTimeStamp(message.getDeleteTimeStamp());
                super.sendToNextNodeHolderActor(super.actor.getNodeIdV(), message, toRelationList, super.actor.getChainIdMap(), super.actor.getActorHolderMap(), false);
            }
            // 所有的上层delete message 都到达后 才会停止此node actor
            if (message.isDeleteFlag()) {
                List<MIotRuleEngineRuleRelationInitV> fromRelationList = super.actor.getFromRelationList();
                fromRelationList.remove(0);
                if (CollectionUtil.isEmpty(fromRelationList)) {
                    super.stopActor(super.actor.self());
                }
            }
        }
    }
    
    public void processNodeNotExistsMessage(NodeNotExistsMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            NodeId nodeIdV = message.getNodeIdV();
            // 删除下层关系 toId有可能是node id 或 rule chain id
            super.deleteToRelationByToNodeId(super.actor.getToRelationList(), nodeIdV, super.getRuleChainIdIfFirstNode(super.actor.getChainIdMap(), nodeIdV));
        }
    }
    
    public void processNodeTriggerMessage(NodeTriggerMessage message) {
        if (!super.checkIfNull(message)) {
            this.processNodeDataMessage(message.getNodeDataMessage());
        }
    }
    
    public void processNodeAlarmMessage(NodeAlarmMessage message) {
        if (!super.checkIfNull(message)) {
            this.processNodeDataMessage(message.getNodeDataMessage());
        }
    }
    
    public void processNodeDataMessage(NodeDataMessage message) {
        if (!super.checkIfNull(message)) {
            // 如果为deadEnd 则为初始化产品时预创建的data check node 此 node 不会做任何check 不需要传递数据
            Boolean deadEnd = super.actor.getConfiguration().getDeadEnd();
            if (null != deadEnd && Boolean.TRUE.equals(deadEnd)) {
                return;
            }
            JSONArray triggerFields = new JSONArray();
            DataCheckResult dataCheckResult;
            
            // 校验单设备预警
            Boolean ifDeviceWarningResult = checkIfDeviceWarning(message);
            
            if (Boolean.TRUE.equals(ifDeviceWarningResult)) {
                // null (表示全部设备) 或 非空集合 并且包含 上报数据设备
                // 数据check
                JSONArray endPointJArray = JSON.parseArray(message.getEndPoints());
                dataCheckResult = dataCheck(endPointJArray, triggerFields);
                if (DataCheckResult.SKIP.equals(dataCheckResult)) {
                    super.hLog.logWhoOccursWhenWarn(super.logWho, "a warning", "process node data message", String.format("endpoint value is null when data check, data: %s, node id: %s", endPointJArray, super.actor.getNodeIdV().getId()));
                    return;
                }
            } else {
                dataCheckResult = DataCheckResult.FALSE;
            }
            
            if (null == dataCheckResult) {
                return;
            }
            String dataCheckResultStr = dataCheckResult.name();
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                List<MIotRuleEngineRuleRelationInitV> filteredToRelationList = toRelationList.stream()
                        .filter(relationInitV -> relationInitV.getRelationType().equalsIgnoreCase(dataCheckResultStr))
                        .collect(Collectors.toList());
                sendToNextNodeHolderActor(
                        super.actor.getNodeIdV(),
                        new NodeTriggerMessage(
                                new NodeId(super.actor.getNodeInitV().getId()),
                                triggerFields,
                                message.getDataTime(),
                                message),
                        filteredToRelationList,
                        super.actor.getChainIdMap(),
                        super.actor.getActorHolderMap(),
                        true
                );
            }
        }
    }
    
    private Boolean checkIfDeviceWarning(NodeDataMessage message) {
        Boolean ifDeviceWarningResult;
        List<String> triggerDeviceIds = super.actor.getConfiguration().getTriggerDeviceIds();
        if (CollectionUtil.isNotEmpty(triggerDeviceIds) && !triggerDeviceIds.contains(message.getDeviceId())) {
            // 非空集合 并且不包含 上报数据设备
            ifDeviceWarningResult = false;
        } else if (null != triggerDeviceIds && CollectionUtil.isEmpty(triggerDeviceIds)) {
            // 空集合 表示不包含任何设备的预警
            ifDeviceWarningResult = false;
        } else {
            ifDeviceWarningResult = true;
        }
        return ifDeviceWarningResult;
    }
    
    private DataCheckResult dataCheck(JSONArray endPointJArray, JSONArray triggerFieldsArray) {
        // max error 为配置的最大错误次数 超过则不会再打印日志
        if (StringUtils.isBlank(super.actor.getConfiguration().getJavaScript()) || jsEvalErrorLogMaxError == 0) {
            return DataCheckResult.SKIP;
        }
        String functionName = super.actor.getNodeIdV().getId();
        if (CollectionUtil.isEmpty(endPointJArray)) {
            return DataCheckResult.SKIP;
        }
        if (!this.jsInvokeService.isComplied(functionName)) {
            super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "data check", "function not complie yet, function: " + functionName);
            return DataCheckResult.SKIP;
        }
        Boolean result = false;
        
        Map<String, JSONObject> endPointNameMap = Maps.newHashMap();
        Bindings bindings = this.prepareBindings(endPointJArray, endPointNameMap);
        
        Object evalResult;
        try {
            evalResult = this.jsInvokeService.invokeFunction(functionName, bindings);
        } catch (FunctionInvokeException e) {
            if (!e.getMessage().startsWith(ConstantFields.REFERENCE_ERROR_JS_EXCEPTION_PREFIX)) {
                super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "eval a JS script", e.getMessage() + " ori js: " + super.actor.getConfiguration().getJavaScript());
                jsEvalErrorLogMaxError--;
            }
            return null;
        }
        if (evalResult instanceof Boolean) {
            result = (Boolean) evalResult;
            if (Boolean.TRUE.equals(result)) {
                result = this.fillTriggerFields(triggerFieldsArray, endPointNameMap, bindings);
            }
        }
        return Boolean.TRUE.equals(result) ? DataCheckResult.TRUE : DataCheckResult.FALSE;
    }
    
    private boolean fillTriggerFields(JSONArray triggerFieldsArray, Map<String, JSONObject> endPointNameMap, Bindings bindings) {
        Set<String> triggerFields;
        try {
            // 获得触发属性
            triggerFields = getTriggerFieldsFromJsScript(super.actor.getConfiguration().getJavaScript(), bindings, endPointNameMap.keySet());
            if (CollectionUtils.isEmpty(triggerFields)) {
                throw new NodeProcessorException("no trigger fields found but not expected");
            }
        } catch (Exception e) {
            super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "get trigger fields from js", e.getMessage() + " ori js: " + super.actor.getConfiguration().getJavaScript());
            return false;
        }
        
        // 加入到触发属性数组
        triggerFields.stream().filter(Objects::nonNull).forEach(triggerField -> {
            if (triggerField.startsWith(ConstantFields.PURE_NUMBER_PREFIX)) {
                triggerField = triggerField.replaceFirst(ConstantFields.PURE_NUMBER_PREFIX, "");
            }
            JSONObject endPointJ = endPointNameMap.get(triggerField);
            if (null == endPointJ) {
                super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "get endpoint jObject from map by trigger field", "endpoint jObject not exists, trigger field: " + triggerField);
            } else {
                triggerFieldsArray.add(endPointJ);
            }
        });
        return true;
    }
    
    /**
     * 如果触发预警 则从js中获得触发的属性
     * 先获得return 后面的语句 再解析语句 拆分所有() 将语句拆分为若干个条件判断语句 再执行每个条件判断 判断哪个为true 筛选出触发的属性
     * @param jsScript
     * @param bindings
     * @param endPointNameSet
     * @return
     * @throws Exception
     */
    private Set<String> getTriggerFieldsFromJsScript(String jsScript, Bindings bindings, Set<String> endPointNameSet) {
        List<String> statementList = getSingleCheckStatement(jsScript);
        
        // 净化每个条件判断语句 替换所有&& || 符号 为 && 并且split by && 获得所有非blank的纯净条件判断语句 最小单元 a > b类似
        List<String> pureStatementList = statementList.parallelStream().map(statement -> {
            String[] pureStatementArray = statement.replace("\\|", "&&").replace("&", "&&").replace("\\|\\|", "&&").split("&&");
            return Lists.newArrayList(pureStatementArray);
        }).collect(Collectors.toList())
                .stream().flatMap(Collection::stream)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toList());
        
        // 执行每个最小单元语句 如果为true 则为触发属性
        Set<String> fieldNameSet = Sets.newHashSet();
        for (String pureStatement : pureStatementList) {
            pureStatement = addParentheses(pureStatement);
            Object result = this.jsInvokeService.eval(pureStatement, bindings);
            if (result instanceof Boolean && Boolean.TRUE.equals(result)) {
                boolean findEndpoint = false;
                for (String endpointName : endPointNameSet) {
                    if (pureStatement.contains(endpointName)) {
                        fieldNameSet.add(endpointName);
                        findEndpoint = true;
                    }
                }
                if (!findEndpoint) {
                    super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "pure statement eval to true", "endpoint name not exists, statement: " + pureStatement + " ori js script: " + jsScript);
                }
            } else {
                throw new NodeProcessorException("pure statement is not eval to a boolean result");
            }
        }
        
        return fieldNameSet;
    }
    
    private List<String> getSingleCheckStatement(String jsScript) {
        jsScript = reduceSemicolon(jsScript);
        
        int loopCount = 0;
        List<String> statementList = Lists.newArrayList();
        boolean loop = true;
        int indexOfLeft = -1;
        // 拆分() 将每个配对的()中的条件判断语句筛选出来
        while (loop) {
            StringBuilder sb = new StringBuilder(jsScript);
            char[] charArray = jsScript.toCharArray();
            boolean needLoop = false;
            for (int i = 0; i < charArray.length; i++) {
                if (charArray[i] == '(') {
                    indexOfLeft = i;
                } else if (charArray[i] == ')') {
                    String statement = jsScript.substring(indexOfLeft + 1, i);
                    statementList.add(statement);
                    jsScript = sb.delete(indexOfLeft, i + 1).toString();
                    needLoop = true;
                    indexOfLeft = -1;
                    break;
                }
            }
            if (!needLoop) {
                if (indexOfLeft != -1) {
                    throw new NodeProcessorException("parse js script error, left any open parentheses");
                }
                loop = false;
            }
            if (++loopCount > 20) {
                throw new NodeProcessorException("substring parentheses loop count > 20");
            }
        }
        // 将拆分到最后剩余的js加入到条件判断语句集合中
        statementList.add(jsScript);
        
        return statementList;
    }
    
    private String reduceSemicolon(String jsScript) {
        // 截取return后的语句
        int returnIndex = jsScript.indexOf("return ");
        if (returnIndex != -1) {
            jsScript = jsScript.substring(returnIndex + 7);
        }
        
        // 去掉句尾分号
        int semicolonIndex;
        int loopCount = 0;
        while ((semicolonIndex = jsScript.lastIndexOf(';')) != -1) {
            jsScript = jsScript.substring(0, semicolonIndex);
            if (++loopCount > 10) {
                throw new NodeProcessorException("substring semicolon loop counts > 10");
            }
        }
        return jsScript;
    }
    
    private String addParentheses(String pureStatement) {
        if (pureStatement.contains(".toLowerCase")) {
            pureStatement = pureStatement.replace(".toLowerCase", ".toLowerCase()").replace("()()", "()");
        }
        return pureStatement;
    }
    
    private Bindings prepareBindings(JSONArray endPointJArray, Map<String, JSONObject> endPointNameMap) {
        Map<String, Object> variableMap = Maps.newHashMap();
        endPointJArray.stream().filter(Objects::nonNull).forEach(endPoint -> {
            if (endPoint instanceof JSONObject) {
                JSONObject endPointJ = (JSONObject) endPoint;
                String endPointName = (String) endPointJ.get(ConstantFields.END_POINT_NAME);
                Object value = endPointJ.get(ConstantFields.VALUE);
                if (StringUtils.isNotBlank(endPointName) && null != value) {
                    endPointNameMap.put(endPointName, endPointJ);
                    if (NumberUtils.isParsable(endPointName)) {
                        endPointName = ConstantFields.PURE_NUMBER_PREFIX + endPointName;
                    }
                    if (value instanceof Boolean) {
                        value = value.toString();
                    }
                    variableMap.put(endPointName, value);
                }
            }
        });
        return this.jsInvokeService.putVariable(variableMap);
    }
    
    public void initScriptEngine() {
        String functionName = super.actor.getNodeInitV().getId();
        String jsScript = super.actor.getConfiguration().getJavaScript();
        if (StringUtils.isNotBlank(jsScript)) {
            try {
                this.jsInvokeService.complie(functionName, jsScript);
            } catch (Exception e) {
                super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "init script engine", e.getMessage());
            }
        }
    }
    
    public void processNodeInitMessage(NodeInitMessage message) {
        if (!super.checkIfNull(message)) {
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                super.sendToNextNodeHolderActor(
                        super.actor.getNodeIdV(),
                        message,
                        toRelationList,
                        super.actor.getChainIdMap(),
                        super.actor.getActorHolderMap(),
                        false
                );
            }
        }
    }
    
    public void postStop() throws ScriptException {
        this.jsInvokeService.releaseFunction(super.actor.getNodeIdV().getId());
    }
}
