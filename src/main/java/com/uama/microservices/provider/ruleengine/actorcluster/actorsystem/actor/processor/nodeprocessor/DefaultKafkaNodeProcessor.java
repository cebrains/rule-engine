package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor;

import com.alibaba.fastjson.JSON;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor.KafkaNodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.ERROR_STR;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 10:29
 **/
public class DefaultKafkaNodeProcessor extends BaseNodeProcessor<KafkaNodeActor> {
    
    public DefaultKafkaNodeProcessor(KafkaNodeActor actor) {
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
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            String jMessage = this.prepareSendingMessage(message);
            this.sendMessage(jMessage);
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                super.sendToNextNodeHolderActor(super.actor.getNodeIdV(), message.getNodeDataMessage(), toRelationList, super.actor.getChainIdMap(), super.actor.getActorHolderMap(), true);
            }
        }
    }
    
    public void processNodeTriggerMessage(NodeTriggerMessage message) {
        if (!super.checkIfNull(message)) {
            this.processNodeAlarmMessage(new NodeAlarmMessage(message.getTriggerNodeIdV(),
                    message.getTriggerFields(), false, message.getTriggerTime(), message.getNodeDataMessage()));
        }
    }
    
    public void processNodeDataMessage(NodeDataMessage message) {
        if (!super.checkIfNull(message)) {
            String jMessage = this.prepareSendingMessage(message);
            this.sendMessage(jMessage);
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                super.sendToNextNodeHolderActor(super.actor.getNodeIdV(), message, toRelationList, super.actor.getChainIdMap(), super.actor.getActorHolderMap(), true);
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
    
    private String prepareSendingMessage(NodeMessage message) {
        KafkaNodeActor.KafkaNodeConfiguration configuration = super.actor.getConfiguration();
        String jMessage = JSON.toJSON(message).toString();
        super.hLog.logWhoOccursWhenInfo(super.logWho, "a info", "prepare sending message", String.format("prepares a message sent to %s topic %s, message: ", configuration.getBootstrapServers(), configuration.getTopicPattern()) + jMessage);
        return jMessage;
    }
    
    private void sendMessage(String jMessage) {
        String topic = super.actor.getConfiguration().getTopicPattern();
        super.actor.getProducer().send(new ProducerRecord<>(topic, jMessage),
                (result, e) -> {
                    if (null == result) {
                        super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "send message", "receive an error after a message send, error: " + e.getMessage());
                    }
                });
    }
}
