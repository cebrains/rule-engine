package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor;

import akka.actor.ActorRef;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.enums.IotRuleRelationToTypeEnum;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.AbstractActorProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageCarrier;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.NodeMessage;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.ERROR_STR;


/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 09:56
 **/
public abstract class BaseNodeProcessor<T extends NodeActor> extends AbstractActorProcessor<T> {
    
    protected final String logWho;
    
    protected BaseNodeProcessor(T actor) {
        super(actor);
        this.logWho = String.format("%s[%s]", actor.getType().getNodeActorClass().getSimpleName(), actor.getNodeIdV().getId());
    }
    
    // node processor do not need a dispatcher
    @Override
    public void dispatchMessage(Object message) {}
    
    protected boolean checkIfNull(NodeMessage message) {
        if (null == message) {
            logReceivedLegalMessageButNull();
        }
        return null == message;
    }
    
    private void logReceivedLegalMessageButNull() {
        this.hLog.logWhoOccursWhenError(this.logWho, ERROR_STR, "receive legal message", "receive a null message");
    }
    
    protected void deleteToRelationByToNodeId(List<MIotRuleEngineRuleRelationInitV> toRelationList, NodeId nodeIdV, String ruleChainId) {
        if (CollectionUtil.isNotEmpty(toRelationList)) {
            // two condition: toId is a rule node id or toId is a rule chain id
            toRelationList.removeIf(toRelation -> toRelation.getToId().equals(nodeIdV.getId()) || toRelation.getToId().equals(ruleChainId));
        }
    }
    
    protected String getRuleChainIdIfFirstNode(Map<String, MIotRuleEngineRuleChainInitV> chainIdMap, NodeId nodeIdV) {
        AtomicReference<String> ruleChainId = new AtomicReference<>("");
        chainIdMap.values().parallelStream().filter(ruleChainInitV -> ruleChainInitV.getFirstNodeId().equals(nodeIdV.getId())).findAny().ifPresent(ruleChainInitV -> ruleChainId.set(ruleChainInitV.getId()));
        return ruleChainId.get();
    }
    
    protected void sendToNextNodeHolderActor(NodeId senderNodeIdV,
                                             NodeMessage message,
                                             List<MIotRuleEngineRuleRelationInitV> toRelationList,
                                             Map<String, MIotRuleEngineRuleChainInitV> chainIdMap,
                                             Map<ActorTypeMapperEnum, ActorRef> actorHolderMap,
                                             boolean sendToNextChain) {
        if (CollectionUtil.isEmpty(toRelationList)) {
            return;
        }
        toRelationList.parallelStream().filter(Objects::nonNull)
                .map(relationInitV -> {
                    if (sendToNextChain && relationInitV.getToType().equals(IotRuleRelationToTypeEnum.RULE_CHAIN.name())) {
                        MIotRuleEngineRuleChainInitV ruleChainInitV = chainIdMap.get(relationInitV.getToId());
                        return new MIotRuleEngineRuleNodeInitV(
                                ruleChainInitV.getFirstNodeId(),
                                ruleChainInitV.getFirstNodeType()
                        );
                    }
                    if (relationInitV.getToType().equals(IotRuleRelationToTypeEnum.RULE_NODE.name())) {
                        return new MIotRuleEngineRuleNodeInitV(
                                relationInitV.getToId(),
                                relationInitV.getToNodeType()
                        );
                    }
                    return null;
                }).parallel()
                .filter(Objects::nonNull)
                .forEach(nodeInitV -> actorHolderMap.get(
                        ActorTypeMapperEnum.valueOf(nodeInitV.getNodeType())
                ).tell(new NodeMessageCarrier<>(new NodeId(nodeInitV.getId()), senderNodeIdV, message), super.actor.self()));
    }
    
    protected void stopActor(ActorRef self) {
        super.actor.context().system().stop(self);
    }
    
    public void processUnknownMessage(Object message) {
        super.logProcessUnknownMessage(this.logWho, message);
    }
}
