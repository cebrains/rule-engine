package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.systemsupplementaryservice;

import akka.actor.ActorSystem;
import akka.actor.DeadLetter;

import java.util.function.Function;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-11 10:18
 **/
public interface SystemSupplementaryService {
    /**
     * 初始化dead letter增强 回调为如何处理dead letter
     * @param actorSystem 嵌入的actor system
     * @param callBack 处理dead letter回调
     */
    void initDeadLetterActor(ActorSystem actorSystem, Function<DeadLetter, Void> callBack);
}
