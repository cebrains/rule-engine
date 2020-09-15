package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor.DefaultMessageTypeSwitchNodeProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:04
 **/
public class MessageTypeSwitchNodeActor extends NodeActor<DefaultMessageTypeSwitchNodeProcessor, MessageTypeSwitchNodeActor.MessageTypeSwitchNodeConfiguration> {
    
    private final MessageTypeSwitchNodeConfiguration configuration;
    
    private Map<String, List<MIotRuleEngineRuleRelationInitV>> relationTypeMap;
    
    protected Object[] args;
    
    public MessageTypeSwitchNodeActor(Map<ActorTypeMapperEnum, ActorRef> actorHolderMap,
                                      Map<String, MIotRuleEngineRuleChainInitV> chainIdMap,
                                      List<MIotRuleEngineRuleRelationInitV> toRelationList,
                                      List<MIotRuleEngineRuleRelationInitV> fromRelationList,
                                      MIotRuleEngineRuleNodeInitV nodeInitV,
                                      Object...args) {
        super(actorHolderMap, chainIdMap, toRelationList, fromRelationList, nodeInitV);
        this.configuration = initNodeConfiguration(nodeInitV);
        this.relationTypeMap = toRelationList.stream().collect(Collectors.groupingBy(MIotRuleEngineRuleRelationInitV::getRelationType));
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
        super.actorProcessor = new DefaultMessageTypeSwitchNodeProcessor(this);
    }
    
    @Override
    protected MessageTypeSwitchNodeConfiguration initNodeConfiguration(MIotRuleEngineRuleNodeInitV nodeInitV) {
        return new MessageTypeSwitchNodeConfiguration();
    }
    
    @Override
    public ActorTypeMapperEnum getType() {
        return ActorTypeMapperEnum.MESSAGE_TYPE_SWITCH;
    }
    
    public Map<String, List<MIotRuleEngineRuleRelationInitV>> getRelationTypeMap() {
        return this.relationTypeMap;
    }
    
    public MessageTypeSwitchNodeConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public static class MessageTypeSwitchNodeConfiguration {
        private MessageTypeSwitchNodeConfiguration() {}
    }
}
