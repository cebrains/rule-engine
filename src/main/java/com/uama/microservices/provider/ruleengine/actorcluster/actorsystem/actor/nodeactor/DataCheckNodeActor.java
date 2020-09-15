package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ConstantFields;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor.DefaultDataCheckNodeProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.List;
import java.util.Map;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.ERROR_STR;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:04
 **/
public class DataCheckNodeActor extends NodeActor<DefaultDataCheckNodeProcessor, DataCheckNodeActor.DataCheckNodeConfiguration> {
    
    private DataCheckNodeConfiguration configuration;
    
    protected Object[] args;
    
    public DataCheckNodeActor(Map<ActorTypeMapperEnum, ActorRef> actorHolderMap,
                              Map<String, MIotRuleEngineRuleChainInitV> chainIdMap,
                              List<MIotRuleEngineRuleRelationInitV> toRelationList,
                              List<MIotRuleEngineRuleRelationInitV> fromRelationList,
                              MIotRuleEngineRuleNodeInitV nodeInitV,
                              Object...args) {
        super(actorHolderMap, chainIdMap, toRelationList, fromRelationList, nodeInitV);
        this.configuration = initNodeConfiguration(nodeInitV);
        this.args = args;
    }
    
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(RootRouterUpdateMessage.class, super.actorProcessor::processRootRouterUpdateMessage)
                .match(NodeDeleteMessage.class, super.actorProcessor::processNodeDeleteMessage)
                .match(NodeNotExistsMessage.class, super.actorProcessor::processNodeNotExistsMessage)
                .match(NodeAlarmMessage.class, super.actorProcessor::processNodeAlarmMessage)
                .match(NodeTriggerMessage.class, super.actorProcessor::processNodeTriggerMessage)
                .match(NodeDataMessage.class, super.actorProcessor::processNodeDataMessage)
                .match(NodeInitMessage.class, super.actorProcessor::processNodeInitMessage)
                .matchAny(super.actorProcessor::processUnknownMessage)
                .build();
    }
    
    @Override
    protected void initActorProcessor() {
        super.actorProcessor = new DefaultDataCheckNodeProcessor(this);
    }
    
    @Override
    public void postStop() throws Exception {
        try {
            super.actorProcessor.postStop();
        } catch (Exception e) {
            super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "release js function", e.getMessage());
        }
        super.postStop();
    }
    
    @Override
    public void preStart() throws Exception {
        super.actorProcessor.initScriptEngine();
        super.preStart();
    }
    
    @Override
    protected DataCheckNodeConfiguration initNodeConfiguration(MIotRuleEngineRuleNodeInitV nodeInitV) {
        JSONObject nodeConfigurationJ = JSON.parseObject(nodeInitV.getNodeConfiguration());
        DataCheckNodeConfiguration checkNodeConfiguration = null;
        if (null != nodeConfigurationJ) {
            String javaScript = (String)nodeConfigurationJ.get(ConstantFields.JS_SCRIPT);
            Boolean deadEnd = (Boolean)nodeConfigurationJ.get(ConstantFields.DEAD_END);
            Object triggerDeviceObj = nodeConfigurationJ.get(ConstantFields.TRIGGER_DEVICE_IDS);
            List<String> triggerDeviceIds = null;
            if (null != triggerDeviceObj) {
                triggerDeviceIds = ((JSONArray)triggerDeviceObj).toJavaList(String.class);
            }
            checkNodeConfiguration = new DataCheckNodeConfiguration(javaScript, deadEnd, triggerDeviceIds);
        }
        return checkNodeConfiguration;
    }
    
    @Override
    public ActorTypeMapperEnum getType() {
        return ActorTypeMapperEnum.DATA_CHECK;
    }
    
    public DataCheckNodeConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public static class DataCheckNodeConfiguration {
        
        private final String javaScript;
        
        private final Boolean deadEnd;
        
        private final List<String> triggerDeviceIds;
        
        private DataCheckNodeConfiguration(String javaScript, Boolean deadEnd, List<String> triggerDeviceIds) {
            this.javaScript = javaScript;
            this.deadEnd = deadEnd;
            this.triggerDeviceIds = triggerDeviceIds;
        }
    
        public String getJavaScript() {
            return javaScript;
        }
    
        public List<String> getTriggerDeviceIds() {
            return triggerDeviceIds;
        }
    
        public Boolean getDeadEnd() {
            return deadEnd;
        }
    }
}
