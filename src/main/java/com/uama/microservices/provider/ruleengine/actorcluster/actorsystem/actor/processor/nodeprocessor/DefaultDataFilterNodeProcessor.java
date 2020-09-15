package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ConstantFields;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor.DataFilterNodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.List;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.PROCESS_STR;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 10:29
 **/
public class DefaultDataFilterNodeProcessor extends BaseNodeProcessor<DataFilterNodeActor> {
    
    public DefaultDataFilterNodeProcessor(DataFilterNodeActor actor) {
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
    
    public void processNodeAlarmMessage(NodeAlarmMessage message) {
        if (!super.checkIfNull(message)) {
            this.processNodeDataMessage(message.getNodeDataMessage());
        }
    }
    
    public void processNodeTriggerMessage(NodeTriggerMessage message) {
        if (!super.checkIfNull(message)) {
            this.processNodeDataMessage(message.getNodeDataMessage());
        }
    }
    
    public void processNodeDataMessage(NodeDataMessage message) {
        if (!super.checkIfNull(message)) {
            DataFilterNodeActor.DataFilterNodeActorConfiguration actorConfiguration = super.actor.getConfiguration();
            String filteredEndPointJStr = ConstantFields.EMPTY_JSON_ARRAY;
            if (null == actorConfiguration) {
                super.hLog.logWhoOccursWhenError(super.logWho, "a warning", String.format(PROCESS_STR, message.getClass().getSimpleName()), "node configuration is null");
            } else {
                filteredEndPointJStr = filterEndPoints(message.getEndPoints(), actorConfiguration.getFilterFieldList());
            }
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                sendToNextNodeHolderActor(
                        super.actor.getNodeIdV(),
                        new NodeDataMessage(
                                message.getDeviceId(),
                                message.getProductId(),
                                filteredEndPointJStr,
                                message.getDataType(),
                                message.getDataTime()),
                        toRelationList,
                        super.actor.getChainIdMap(),
                        super.actor.getActorHolderMap(),
                        true
                );
            }
        }
    }
    
    private String filterEndPoints(String endPoints, List<String> filterFieldList) {
        String filteredEndPointJStr = ConstantFields.EMPTY_JSON_ARRAY;
        JSONArray endPointJArray = JSON.parseArray(endPoints);
        
        if (CollectionUtil.isNotEmpty(filterFieldList) && CollectionUtil.isNotEmpty(endPointJArray)) {
            JSONArray filteredEndPointJArray = new JSONArray();
            endPointJArray.forEach(endPoint -> {
                if (endPoint instanceof JSONObject) {
                    JSONObject endPointJ = (JSONObject)endPoint;
                    String endPointName = (String) endPointJ.get(ConstantFields.END_POINT_NAME);
                    if (filterFieldList.contains(endPointName)) {
                        JSONObject filteredJ = new JSONObject().fluentPutAll(endPointJ);
                        filteredEndPointJArray.add(filteredJ);
                    }
                }
            });
            filteredEndPointJStr = filteredEndPointJArray.toJSONString();
        }
        return filteredEndPointJStr;
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
}
