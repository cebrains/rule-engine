package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor;

import akka.actor.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ActorSystemContext;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.intf.ActorRefCreator;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.intf.NodeTypedActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.ActorProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeLifeCycleInfo;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageCarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 21:07
 **/
public abstract class NodeHolderActor<P extends ActorProcessor> extends AbstractActor implements NodeTypedActor {
    
    private final Logger logger = LoggerFactory.getLogger(NodeHolderActor.class);
    
    private final BiMap<NodeId, ActorRef> nodeIdBiMap;
    
    private final ActorSystemContext actorSystemContext;
    
    private final SupervisorStrategy strategy;
    
    protected P actorProcessor;
    
    private final Map<NodeId, List<NodeMessageCarrier>> lockedFirstNodeIdVMap;
    
    private final Map<NodeId, NodeLifeCycleInfo> nodeLifeCycleMap;
    
    public NodeHolderActor(ActorSystemContext actorSystemContext, BiMap<NodeId, ActorRef> nodeIdBiMap, Map<NodeId, List<NodeMessageCarrier>> lockedFirstNodeIdVMap, Map<NodeId, NodeLifeCycleInfo> nodeLifeCycleMap) {
        this.actorSystemContext = actorSystemContext;
        this.nodeIdBiMap = nodeIdBiMap;
        this.lockedFirstNodeIdVMap = lockedFirstNodeIdVMap;
        this.nodeLifeCycleMap = nodeLifeCycleMap;
        //holder actor 对其子actor的监管策略
        this.strategy = new OneForOneStrategy(3, Duration.create("1 minute"), t -> {
            this.logger.error("Sub-actor unknown failure:", t);
            if (t instanceof RuntimeException) {
                return SupervisorStrategy.resume();
            } else {
                return SupervisorStrategy.stop();
            }
        });
        initActorProcessor();
    }
    
    protected abstract void initActorProcessor();
    
    public static class ActorCreator implements ActorRefCreator {
        
        private final ActorSystemContext actorSystemContext;
        
        private final ActorTypeMapperEnum actorTypeMapperEnum;
    
        public ActorCreator(ActorSystemContext actorSystemContext, ActorTypeMapperEnum actorTypeMapperEnum) {
            this.actorSystemContext = actorSystemContext;
            this.actorTypeMapperEnum = actorTypeMapperEnum;
        }
    
        @Override
        public ActorRef create() {
            return actorSystemContext
                    .getActorSystem()
                    .actorOf(
                            Props.create(actorTypeMapperEnum.getNodeHolderActorClass(), actorSystemContext, HashBiMap.create(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>())
                                    .withDispatcher(actorTypeMapperEnum.getDispatcherName()),
                            actorTypeMapperEnum.getNodeHolderActorClass().getSimpleName()
                    );
        }
    }
    
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }
    
    @Override
    public void preStart() throws Exception {
        this.logger.info("Actor {} up", getType().getNodeHolderActorClass().getSimpleName());
        super.preStart();
    }
    
    @Override
    public void postStop() throws Exception {
        this.logger.info("Actor {} stop", getType().getNodeHolderActorClass().getSimpleName());
        super.postStop();
    }
    
    public Logger getLogger() {
        return this.logger;
    }
    
    public ActorSystemContext getActorSystemContext() {
        return actorSystemContext;
    }
    
    public BiMap<NodeId, ActorRef> getNodeIdBiMap() {
        return nodeIdBiMap;
    }
    
    public Map<NodeId, List<NodeMessageCarrier>> getLockedFirstNodeIdVMap() {
        return lockedFirstNodeIdVMap;
    }
    
    public Map<NodeId, NodeLifeCycleInfo> getNodeLifeCycleMap() {
        return nodeLifeCycleMap;
    }
}
