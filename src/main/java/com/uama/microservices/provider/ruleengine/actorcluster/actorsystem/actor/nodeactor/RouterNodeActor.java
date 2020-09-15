package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor.DefaultRouterNodeProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.List;
import java.util.Map;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:04
 **/
public class RouterNodeActor extends NodeActor<DefaultRouterNodeProcessor, RouterNodeActor.RouterNodeConfiguration> {
    
    private final RouterNodeConfiguration configuration;
    
    private Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap;
    
    public RouterNodeActor(Map<ActorTypeMapperEnum, ActorRef> actorHolderMap,
                           Map<String, MIotRuleEngineRuleChainInitV> chainIdMap,
                           List<MIotRuleEngineRuleRelationInitV> toRelationList,
                           List<MIotRuleEngineRuleRelationInitV> fromRelationList,
                           MIotRuleEngineRuleNodeInitV nodeInitV,
                           Object... args) {
        super(actorHolderMap, chainIdMap, toRelationList, fromRelationList, nodeInitV);
        this.configuration = initNodeConfiguration(nodeInitV);
        if (args.length > 0 && args[0] instanceof Map) {
            this.chainsGroupByBelongToIdMap = (Map<String, List<MIotRuleEngineRuleChainInitV>>) args[0];
        }
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
        super.actorProcessor = new DefaultRouterNodeProcessor(this);
    }
    
    @Override
    protected RouterNodeConfiguration initNodeConfiguration(MIotRuleEngineRuleNodeInitV nodeInitV) {
        return new RouterNodeConfiguration();
    }
    
    @Override
    public ActorTypeMapperEnum getType() {
        return ActorTypeMapperEnum.ROUTER;
    }
    
    public Map<String, List<MIotRuleEngineRuleChainInitV>> getChainsGroupByBelongToIdMap() {
        return this.chainsGroupByBelongToIdMap;
    }
    
    public RouterNodeConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public static class RouterNodeConfiguration {
        private RouterNodeConfiguration() {}
    }
}
