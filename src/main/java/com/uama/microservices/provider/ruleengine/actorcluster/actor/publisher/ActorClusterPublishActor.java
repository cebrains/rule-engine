package com.uama.microservices.provider.ruleengine.actorcluster.actor.publisher;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.HumanReadableLog;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.impl.DefaultHumanReadableLog;
import com.uama.microservices.provider.ruleengine.actorcluster.message.BroadCastMessage;
import com.uama.microservices.provider.ruleengine.actorcluster.message.ClusterStatsMessage;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.ERROR_STR;
import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.PROCESS_STR;
import static com.uama.microservices.provider.ruleengine.actorcluster.message.ActorClusterMessageType.CLUSTER_LEAVE;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-09-02 18:22
 **/
public class ActorClusterPublishActor extends AbstractActor {
    private final ActorRef mediator;
    
    private final HumanReadableLog hLog = new DefaultHumanReadableLog();
    
    public ActorClusterPublishActor(ActorSystem actorSystem) {
        mediator = DistributedPubSub.get(actorSystem).mediator();
    }
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BroadCastMessage.class, broadCastMessage -> mediator.tell(new DistributedPubSubMediator.Publish(broadCastMessage.getMessageType().name(), broadCastMessage), getSelf()))
                .match(ClusterStatsMessage.class, message -> {
                    if (CLUSTER_LEAVE.equals(message.getMessageType())) {
                        getSelf().tell(PoisonPill.getInstance(), getSelf());
                    } else {
                        hLog.logWhoOccursWhenError(this.getClass().getSimpleName(), ERROR_STR, "match cluster stats message type", "not found proper type");
                    }
                })
                .matchAny(message -> hLog.logWhoOccursWhenError(this.getClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "receive an unknown message: " + message))
                .build();
    }
}
