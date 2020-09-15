package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor;

import akka.actor.ActorRef;
import com.google.common.collect.Lists;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor.RouterNodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageCarrier;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.*;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 10:29
 **/
public class DefaultRouterNodeProcessor extends BaseNodeProcessor<RouterNodeActor> {
    
    public DefaultRouterNodeProcessor(RouterNodeActor actor) {
        super(actor);
    }
    
    public void processRootRouterUpdateMessage(RootRouterUpdateMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            List<MIotRuleEngineRuleChainInitV> ruleChainVList = message.getRuleChainVList();
            Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap = super.actor.getChainsGroupByBelongToIdMap();
            if (CollectionUtil.isNotEmpty(ruleChainVList)) {
                ruleChainVList.stream().filter(Objects::nonNull).forEach(ruleChain -> {
                    String productId = ruleChain.getBelongToId();
                    if (StringUtils.isNotBlank(productId)) {
                        List<MIotRuleEngineRuleChainInitV> computeVList = chainsGroupByBelongToIdMap.computeIfPresent(productId, (k, v) -> {
                            v.removeIf(element -> element.getId().equals(ruleChain.getId()));
                            v.add(ruleChain);
                            return v;
                        });
                        if (null == computeVList) {
                            chainsGroupByBelongToIdMap.put(productId, Lists.newArrayList(ruleChain));
                        }
                    }
                });
            }
            super.hLog.logWhoOccursWhenDebug(super.logWho, DEBUG_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "update route path complete, now route path: " + chainsGroupByBelongToIdMap);
            
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                super.sendToNextNodeHolderActor(super.actor.getNodeIdV(), message, toRelationList, super.actor.getChainIdMap(), super.actor.getActorHolderMap(), false);
            }
        }
    }
    
    public void processNodeDeleteMessage(NodeDeleteMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            if (!message.isRoot()) {
                sendDeleteMessageToProductRuleChains(message);
            }
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
    
    private void sendDeleteMessageToProductRuleChains(NodeDeleteMessage message) {
        String productId = message.getRuleChainV().getBelongToId();
        Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap = super.actor.getChainsGroupByBelongToIdMap();
        if (StringUtils.isNotBlank(productId)) {
            if (!chainsGroupByBelongToIdMap.containsKey(productId)) {
                super.hLog.logWhoOccursWhenDebug(super.logWho, DEBUG_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "receive a node delete message but not found productId " + productId);
            } else {
                List<MIotRuleEngineRuleChainInitV> toRuleChainList = chainsGroupByBelongToIdMap.get(productId);
                if (CollectionUtil.isNotEmpty(toRuleChainList)) {
                    this.dispatchRuleChains(super.actor.getNodeIdV(),
                            new NodeDeleteMessage(message.getRuleChainV(), true, message.isRoot(), System.currentTimeMillis()),
                            toRuleChainList,
                            super.actor.getActorHolderMap());
                }
                chainsGroupByBelongToIdMap.remove(productId);
            }
        }
    }
    
    public void processNodeNotExistsMessage(NodeNotExistsMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            NodeId nodeIdV = message.getNodeIdV();
            this.deleteProductRuleChainsByToNodeId(nodeIdV);
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
            String productId = message.getProductId();
            if (StringUtils.isBlank(productId)) {
                super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "message product id is blank");
                return;
            }
            Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap = super.actor.getChainsGroupByBelongToIdMap();
            if (!chainsGroupByBelongToIdMap.containsKey(productId)) {
                super.hLog.logWhoOccursWhenWarn(super.logWho, "a warning", String.format(PROCESS_STR, message.getClass().getSimpleName()), "product id have no chain");
                return;
            }
            List<MIotRuleEngineRuleChainInitV> toRuleChainList = chainsGroupByBelongToIdMap.get(productId);
            if (CollectionUtil.isNotEmpty(toRuleChainList)) {
                this.dispatchRuleChains(super.actor.getNodeIdV(), message, toRuleChainList, super.actor.getActorHolderMap());
            }
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                super.sendToNextNodeHolderActor(super.actor.getNodeIdV(), message, toRelationList, super.actor.getChainIdMap(), super.actor.getActorHolderMap(), true);
            }
        }
    }
    
    private void deleteProductRuleChainsByToNodeId(NodeId nodeIdV) {
        List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
        // get toRelation by target nodeId due to toRelation.toId = nodeId
        Optional<MIotRuleEngineRuleRelationInitV> toRelationOpt = toRelationList.parallelStream().filter(toRelation -> toRelation.getToId().equals(nodeIdV.getId())).findAny();
        toRelationOpt.ifPresent(toRelation -> {
            // get target node's ruleChainId
            String ruleChainId = toRelation.getRuleChainId();
            Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap = super.actor.getChainsGroupByBelongToIdMap();
            // get ruleChain's belongToId (productId)
            Optional<MIotRuleEngineRuleChainInitV> ruleChainInitVOpt = chainsGroupByBelongToIdMap.values().parallelStream().flatMap(Collection::stream).filter(ruleChainInitV -> ruleChainInitV.getId().equals(ruleChainId)).findAny();
            ruleChainInitVOpt.ifPresent(ruleChainInitV -> {
                String productId = ruleChainInitV.getBelongToId();
                // remove product rule chains
                if (StringUtils.isNotBlank(productId)) {
                    chainsGroupByBelongToIdMap.remove(productId);
                }
            });
        });
    }
    
    private void dispatchRuleChains(NodeId senderNodeIdV,
                                    NodeMessage message,
                                    List<MIotRuleEngineRuleChainInitV> toRuleChainList,
                                    Map<ActorTypeMapperEnum, ActorRef> actorHolderMap) {
        if (CollectionUtil.isEmpty(toRuleChainList)) {
            return;
        }
        toRuleChainList.parallelStream()
                .forEach(nodeInitV -> {
                    String nodeType = nodeInitV.getFirstNodeType();
                    String nodeId = nodeInitV.getFirstNodeId();
                    ActorRef nextHolderActorRef = actorHolderMap.get(
                            ActorTypeMapperEnum.valueOf(nodeType)
                    );
                    super.hLog.logWhoOccursWhenDebug(super.logWho, DEBUG_STR, "dispatch rule chains", String.format("send a message to actor holder %s, node id %s, message: ", nodeType, nodeId) + message);
                    nextHolderActorRef.tell(new NodeMessageCarrier<>(new NodeId(nodeId), senderNodeIdV, message), super.actor.self());
                });
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
