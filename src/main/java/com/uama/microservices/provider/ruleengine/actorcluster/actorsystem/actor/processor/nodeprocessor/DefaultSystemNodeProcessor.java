package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor;

import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor.SystemNodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageCarrier;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.ERROR_STR;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 10:29
 **/
public class DefaultSystemNodeProcessor extends BaseNodeProcessor<SystemNodeActor> {
    
    public DefaultSystemNodeProcessor(SystemNodeActor actor) {
        super(actor);
    }
    
    public void processRootRouterUpdateMessage(RootRouterUpdateMessage message) {
        if (!super.checkIfNull(message)) {
            forwardMessage(message);
        }
    }
    
    public void processNodeDeleteMessage(NodeDeleteMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            forwardMessage(message);
            if (message.isDeleteFlag()) {
                super.stopActor(super.actor.self());
            }
        }
    }
    
    public void processNodeNotExistsMessage(NodeNotExistsMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            NodeId nodeIdV = message.getNodeIdV();
            // toRelationList is always empty
            super.deleteToRelationByToNodeId(super.actor.getToRelationList(), nodeIdV, super.getRuleChainIdIfFirstNode(super.actor.getChainIdMap(), nodeIdV));
        }
    }
    
    public void processNodeAlarmMessage(NodeAlarmMessage message) {
        if (!super.checkIfNull(message)) {
            forwardMessage(message.getNodeDataMessage());
        }
    }
    
    public void processNodeTriggerMessage(NodeTriggerMessage message) {
        if (!super.checkIfNull(message)) {
            forwardMessage(message.getNodeDataMessage());
        }
    }
    
    public void processNodeDataMessage(NodeDataMessage message) {
        if (!super.checkIfNull(message)) {
            forwardMessage(message);
        }
    }
    
    private void forwardMessage(NodeMessage message) {
        MIotRuleEngineRuleNodeInitV nodeInitV = super.actor.getNodeInitV();
        if (null != nodeInitV) {
            super.actor.getActorHolderMap()
                    .get(ActorTypeMapperEnum.valueOf(nodeInitV.getNodeType()))
                    .tell(new NodeMessageCarrier<>(new NodeId(nodeInitV.getId()), super.actor.getNodeIdV(), message), super.actor.self());
        }
    }
    
    public void processNodeInitMessage(NodeInitMessage message) {
        if (!super.checkIfNull(message)) {
            super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "processe a node init message", "Actor {} received a node init message, but not expect, message: " + message);
        }
    }
}
