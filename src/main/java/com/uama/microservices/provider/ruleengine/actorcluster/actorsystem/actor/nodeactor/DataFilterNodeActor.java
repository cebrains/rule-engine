package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.alibaba.fastjson.JSON;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor.DefaultDataFilterNodeProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.List;
import java.util.Map;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:04
 **/
public class DataFilterNodeActor extends NodeActor<DefaultDataFilterNodeProcessor, DataFilterNodeActor.DataFilterNodeActorConfiguration> {
    
    private final DataFilterNodeActorConfiguration configuration;
    
    protected Object[] args;
    
    public DataFilterNodeActor(Map<ActorTypeMapperEnum, ActorRef> actorHolderMap,
                               Map<String, MIotRuleEngineRuleChainInitV> chainIdMap,
                               List<MIotRuleEngineRuleRelationInitV> toRelationList,
                               List<MIotRuleEngineRuleRelationInitV> fromRelationList,
                               MIotRuleEngineRuleNodeInitV nodeInitV,
                               Object... args) {
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
        super.actorProcessor = new DefaultDataFilterNodeProcessor(this);
    }
    
    @Override
    protected DataFilterNodeActorConfiguration initNodeConfiguration(MIotRuleEngineRuleNodeInitV nodeInitV) {
        return JSON.parseObject(nodeInitV.getNodeConfiguration(), DataFilterNodeActorConfiguration.class);
    }
    
    @Override
    public ActorTypeMapperEnum getType() {
        return ActorTypeMapperEnum.DATA_FILTER;
    }
    
    public DataFilterNodeActorConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public static class DataFilterNodeActorConfiguration {
        
        private List<String> filterFieldList;
        
        public List<String> getFilterFieldList() {
            return filterFieldList;
        }
    
        public void setFilterFieldList(List<String> filterFieldList) {
            this.filterFieldList = filterFieldList;
        }
    }
}
