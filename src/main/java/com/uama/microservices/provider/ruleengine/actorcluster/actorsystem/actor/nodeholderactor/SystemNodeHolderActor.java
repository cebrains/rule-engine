package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeholderactor;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.collect.BiMap;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ActorSystemContext;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeHolderActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeholderprocessor.DefaultSystemNodeHolderProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeLifeCycleInfo;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.SystemRuleChainContext;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageCarrier;
import com.uama.microservices.provider.ruleengine.support.TimeUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 22:12
 **/
public class SystemNodeHolderActor extends NodeHolderActor<DefaultSystemNodeHolderProcessor> {
    
    private static final AtomicLong processedCounts = new AtomicLong(0);
    private static final AtomicLong maxAverageExcutionRage = new AtomicLong(0);
    private static final AtomicLong lastLogSeconds = new AtomicLong(0);
    private static final AtomicLong lastSecondCount = new AtomicLong(0);
    
    private static final Map<String, SystemRuleChainContext> ruleChainIdContextMap = new ConcurrentHashMap<>();
    
    public SystemNodeHolderActor(ActorSystemContext actorSystemContext, BiMap<NodeId, ActorRef> nodeIdBiMap, Map<NodeId, List<NodeMessageCarrier>> lockedFirstNodeIdVMap, Map<NodeId, NodeLifeCycleInfo> nodeLifeCycleMap) {
        super(actorSystemContext, nodeIdBiMap, null, nodeLifeCycleMap);
    }
    
    @Override
    public void initActorProcessor() {
        super.actorProcessor = new DefaultSystemNodeHolderProcessor(this);
    }
    
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(NodeMessageCarrier.class, messageCarrier -> {
                    this.logActorProcessedStatus();
                    super.actorProcessor.dispatchMessage(messageCarrier);
                })
                .matchAny(super.actorProcessor::processUnknownMessage)
                .build();
    }
    
    private void logActorProcessedStatus() {
        long processedCount = SystemNodeHolderActor.processedCounts.incrementAndGet();
        long runningSeconds = this.context().system().uptime();
        long unLogSeconds = runningSeconds - SystemNodeHolderActor.lastLogSeconds.get();
        if (unLogSeconds >= 1) {
            Long nowExcutionRate = processedCount - lastSecondCount.get();
            lastSecondCount.set(processedCount);
            if (nowExcutionRate > SystemNodeHolderActor.maxAverageExcutionRage.get()) {
                SystemNodeHolderActor.maxAverageExcutionRage.set(nowExcutionRate);
            }
            String runningTimeStr = TimeUtils.secondToRunningTime(runningSeconds);
            SystemNodeHolderActor.lastLogSeconds.set(runningSeconds);
            super.getLogger().info("Actor {} processed {} messages, running {}, max processed rate per second: {}",
                    this.getType().getNodeHolderActorClass().getSimpleName(),
                    processedCount,
                    runningTimeStr,
                    SystemNodeHolderActor.maxAverageExcutionRage.get());
        }
    }
    
    public static Map<String, SystemRuleChainContext> ruleChainIdContextMap() {
        return ruleChainIdContextMap;
    }
    
    @Override
    public ActorTypeMapperEnum getType() {
        return ActorTypeMapperEnum.SYSTEM;
    }
}
