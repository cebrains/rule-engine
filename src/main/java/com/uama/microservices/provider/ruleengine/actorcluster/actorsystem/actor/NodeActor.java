package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.intf.NodeTypedActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.AbstractActorProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.HumanReadableLog;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.impl.DefaultHumanReadableLog;

import java.util.List;
import java.util.Map;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.DEBUG_STR;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-10 09:24
 **/
public abstract class NodeActor<P extends AbstractActorProcessor, C> extends AbstractActor implements NodeTypedActor {
    
    protected final HumanReadableLog hLog;
    
    protected final String logWho;
    
    private final Map<ActorTypeMapperEnum, ActorRef> actorHolderMap;
    
    private final Map<String, MIotRuleEngineRuleChainInitV> chainIdMap;
    
    private final List<MIotRuleEngineRuleRelationInitV> toRelationList;
    
    private final List<MIotRuleEngineRuleRelationInitV> fromRelationList;
    
    private final MIotRuleEngineRuleNodeInitV nodeInitV;
    
    private final NodeId nodeIdV;
    
    protected P actorProcessor;
    
    public NodeActor(Map<ActorTypeMapperEnum, ActorRef> actorHolderMap,
                     Map<String, MIotRuleEngineRuleChainInitV> chainIdMap,
                     List<MIotRuleEngineRuleRelationInitV> toRelationList,
                     List<MIotRuleEngineRuleRelationInitV> fromRelationList,
                     MIotRuleEngineRuleNodeInitV nodeInitV) {
        this.actorHolderMap = actorHolderMap;
        this.chainIdMap = chainIdMap;
        this.toRelationList = toRelationList;
        this.fromRelationList = fromRelationList;
        this.nodeInitV = nodeInitV;
        this.nodeIdV = new NodeId(nodeInitV.getId());
        this.hLog = new DefaultHumanReadableLog();
        this.logWho = String.format("%s[%s]", this.getType().getNodeActorClass().getSimpleName(), nodeInitV.getId());
        initActorProcessor();
    }
    
    protected abstract void initActorProcessor();
    
    protected abstract C initNodeConfiguration(MIotRuleEngineRuleNodeInitV nodeInitV);
    
    @Override
    public void preStart() throws Exception {
        this.hLog.logWhoOccursWhenDebug(this.logWho, DEBUG_STR, "start actor", "Actor up");
        super.preStart();
    }
    
    @Override
    public void postStop() throws Exception {
        this.hLog.logWhoOccursWhenDebug(this.logWho, DEBUG_STR, "stop actor", "Actor stop");
        super.postStop();
    }
    
    public List<MIotRuleEngineRuleRelationInitV> getToRelationList() {
        return toRelationList;
    }
    
    public List<MIotRuleEngineRuleRelationInitV> getFromRelationList() {
        return fromRelationList;
    }
    
    public MIotRuleEngineRuleNodeInitV getNodeInitV() {
        return nodeInitV;
    }
    
    public NodeId getNodeIdV() {
        return nodeIdV;
    }
    
    public Map<ActorTypeMapperEnum, ActorRef> getActorHolderMap() {
        return actorHolderMap;
    }
    
    public Map<String, MIotRuleEngineRuleChainInitV> getChainIdMap() {
        return chainIdMap;
    }
    
    public long getDeleteTimeStamp() {
        return this.nodeInitV.getDeleteTimeStamp();
    }
    
    public void setDeleteTimeStamp(long deleteTimeStamp) {
        this.nodeInitV.setDeleteTimeStamp(deleteTimeStamp);
    }
}
