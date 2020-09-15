package com.uama.microservices.provider.ruleengine.actorcluster;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.CoordinatedShutdown;
import akka.actor.Props;
import com.uama.microservices.provider.ruleengine.actorcluster.actor.ActorTypeEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actor.publisher.ActorClusterPublishActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actor.receiver.rulechain.UpdateRuleChainCacheActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice.ActorService;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.HumanReadableLog;
import com.uama.microservices.provider.ruleengine.actorcluster.message.ActorClusterMessageType;
import com.uama.microservices.provider.ruleengine.actorcluster.message.BroadCastMessage;
import com.uama.microservices.provider.ruleengine.actorcluster.message.ClusterStatsMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-09-02 17:23
 **/
@Service
@ConditionalOnProperty(value = "actor.actor-cluster.enable", havingValue = "true")
public class DefaultActorClusterService implements ActorClusterService {
    
    @Autowired
    private ActorService actorService;
    
    @Autowired
    private ActorClusterContext actorClusterContext;
    
    @Autowired
    private HumanReadableLog hLog;
    
    @PostConstruct
    public void init() {
        ActorSystem actorSystem = actorService.getActorSystemContext().getActorSystem();
        initReceiver(actorSystem);
        initPublisher(actorSystem);
        hookShutDown(actorSystem);
    }
    
    private void hookShutDown(ActorSystem actorSystem) {
        CoordinatedShutdown.get(actorSystem)
                .addTask(CoordinatedShutdown.PhaseBeforeClusterShutdown(),
                        "stop subscribers",
                        () -> {
                            CompletableFuture<Done> f = new CompletableFuture<>();
                            this.actorClusterContext.getActorBiMap().values().parallelStream().forEach(actorRef -> actorRef.tell(new ClusterStatsMessage(ActorClusterMessageType.CLUSTER_LEAVE), ActorRef.noSender()));
                            this.actorClusterContext.getActorBiMap().clear();
                            f.complete(Done.getInstance());
                            return f;
                        });
    }
    
    private void initPublisher(ActorSystem actorSystem) {
        ActorRef publisher = actorSystem.actorOf(Props.create(ActorClusterPublishActor.class, actorSystem));
        this.actorClusterContext.getActorBiMap().put(ActorTypeEnum.PUBLISHER, publisher);
    }
    
    private void initReceiver(ActorSystem actorSystem) {
        ActorRef updateRuleChainCacheReceiver = actorSystem.actorOf(Props.create(UpdateRuleChainCacheActor.class, actorSystem, actorService));
        this.actorClusterContext.getActorBiMap().put(ActorTypeEnum.UPDATE_RULE_CHAIN_CACHE_RECEIVER, updateRuleChainCacheReceiver);
    }
    
    @Override
    public void broadCast(BroadCastMessage message) {
        ActorRef publisher = this.actorClusterContext.getActorBiMap().get(ActorTypeEnum.PUBLISHER);
        if (null != publisher) {
            publisher.tell(message, ActorRef.noSender());
        } else {
            hLog.logWhoOccursWhenWarn(this.getClass().getSimpleName(), "a warning", String.format("broadcast message %s", message), "publisher is null");
        }
    }
}
