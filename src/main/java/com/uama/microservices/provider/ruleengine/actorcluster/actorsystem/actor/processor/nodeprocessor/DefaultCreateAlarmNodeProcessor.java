package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor;

import com.alibaba.fastjson.JSONArray;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor.CreateAlarmNodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.Date;
import java.util.List;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 10:29
 **/
public class DefaultCreateAlarmNodeProcessor extends BaseNodeProcessor<CreateAlarmNodeActor> {
    
    public DefaultCreateAlarmNodeProcessor(CreateAlarmNodeActor actor) {
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
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                sendToNextNodeHolderActor(
                        super.actor.getNodeIdV(),
                        message,
                        toRelationList,
                        super.actor.getChainIdMap(),
                        super.actor.getActorHolderMap(),
                        true
                );
            }
        }
    }
    
    public void processNodeTriggerMessage(NodeTriggerMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            NodeAlarmMessage alarmMessage = constructAlarmMessage(message);
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                sendToNextNodeHolderActor(
                        super.actor.getNodeIdV(),
                        alarmMessage,
                        toRelationList,
                        super.actor.getChainIdMap(),
                        super.actor.getActorHolderMap(),
                        true
                );
            }
        }
    }
    
    public void processNodeDataMessage(NodeDataMessage message) {
        if (!super.checkIfNull(message)) {
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                sendToNextNodeHolderActor(
                        super.actor.getNodeIdV(),
                        constructAlarmMessage(message),
                        toRelationList,
                        super.actor.getChainIdMap(),
                        super.actor.getActorHolderMap(),
                        true
                );
            }
        }
    }
    
    private NodeAlarmMessage constructAlarmMessage(NodeMessage message) {
        if (message instanceof NodeTriggerMessage) {
            NodeTriggerMessage triggerMessage = (NodeTriggerMessage)message;
            JSONArray triggerFields = triggerMessage.getTriggerFields();
            boolean alert = CollectionUtil.isNotEmpty(triggerFields);
            return new NodeAlarmMessage(triggerMessage.getTriggerNodeIdV(), triggerFields, alert, triggerMessage.getTriggerTime(), triggerMessage.getNodeDataMessage());
        } else if (message instanceof NodeDataMessage) {
            NodeDataMessage dataMessage = (NodeDataMessage)message;
            return new NodeAlarmMessage(new NodeId(super.actor.getNodeInitV().getId()), new JSONArray(), false, new Date(), dataMessage);
        }
        return null;
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
