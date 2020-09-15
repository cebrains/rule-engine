package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.intf.NodeTypedActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.NodeMessage;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.HumanReadableLog;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.impl.DefaultHumanReadableLog;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.*;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-31 15:45
 **/
public abstract class AbstractActorProcessor<T extends NodeTypedActor> implements ActorProcessor{
    
    protected final HumanReadableLog hLog;
    protected final T actor;
    
    protected AbstractActorProcessor(T actor) {
        this.hLog = new DefaultHumanReadableLog();
        this.actor = actor;
    }
    
    protected void logRuleChainContextNotExists(String who, Object message, String ruleChainId) {
        this.hLog.logWhoOccursWhenError(who, ERROR_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "rule chain[" + ruleChainId + "] context not exists");
    }
    
    protected void logNodeLifeCycleIsNull(String who, Object message, NodeId nodeIdV) {
        this.hLog.logWhoOccursWhenError(who, ERROR_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "node[" + nodeIdV.getId() + "] have no life cycle info");
    }
    
    protected void logReceivedIllegalMessage(String who, Object message) {
        this.hLog.logWhoOccursWhenError(who, ERROR_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "receive an illegal message: " + message);
    }
    
    protected void logReceivedSomeKindOfMessage(String who, NodeMessage message) {
        this.hLog.logWhoOccursWhenDebug(who, DEBUG_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "receive a message: " + message);
    }
    
    protected void logProcessUnknownMessage(String who, Object message) {
        this.hLog.logWhoOccursWhenError(who, ERROR_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "receive an unknown message: " + message);
    }
}
