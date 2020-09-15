package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.systemsupplementaryservice.impl;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.systemsupplementaryservice.AbstractSystemSupplementaryService;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.systemsupplementaryservice.actor.DeadLetterActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.systemsupplementaryservice.actor.SystemSupplementaryType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-11 10:20
 **/
@Service
public class DefaultSystemSupplementaryService extends AbstractSystemSupplementaryService {
    
    static final Map<SystemSupplementaryType, ActorRef> actorMap = new ConcurrentHashMap<>();
    
    @Override
    protected void doInitDeadLetterActor(ActorSystem actorSystem, Function<DeadLetter, Void> callBack) {
        // 生成deadLetter监听actor
        ActorRef deadLetterActor = actorSystem.actorOf(Props.create(DeadLetterActor.class, callBack));
        // 订阅actor system中的dead letter消息
        actorSystem.getEventStream().subscribe(deadLetterActor, DeadLetter.class);
        DefaultSystemSupplementaryService.actorMap.put(SystemSupplementaryType.DEAD_LETTER, deadLetterActor);
    }
}
