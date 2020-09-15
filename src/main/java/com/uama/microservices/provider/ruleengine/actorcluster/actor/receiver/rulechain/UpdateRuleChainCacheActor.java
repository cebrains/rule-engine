package com.uama.microservices.provider.ruleengine.actorcluster.actor.receiver.rulechain;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.google.common.collect.Lists;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice.ActorService;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.HumanReadableLog;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.impl.DefaultHumanReadableLog;
import com.uama.microservices.provider.ruleengine.actorcluster.message.ActorClusterMessageType;
import com.uama.microservices.provider.ruleengine.actorcluster.message.BroadCastMessage;
import com.uama.microservices.provider.ruleengine.actorcluster.message.ClusterStatsMessage;

import java.util.List;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.ERROR_STR;
import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.PROCESS_STR;
import static com.uama.microservices.provider.ruleengine.actorcluster.message.ActorClusterMessageType.CLUSTER_LEAVE;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-09-02 18:01
 **/
public class UpdateRuleChainCacheActor extends AbstractActor {
    
    private final ActorRef mediator;
    
    private final ActorService actorService;
    
    private final HumanReadableLog hLog = new DefaultHumanReadableLog();
    
    private final List<ActorClusterMessageType> subscribeList = Lists.newArrayList();
    
    public UpdateRuleChainCacheActor(ActorSystem actorSystem, ActorService actorService) {
        this.mediator = DistributedPubSub.get(actorSystem).mediator();
        this.actorService = actorService;
    }
    
    @Override
    public void preStart() throws Exception {
        mediator.tell(new DistributedPubSubMediator.Subscribe(ActorClusterMessageType.RULE_CHAIN_INSERT.name(), getSelf()), getSelf());
        mediator.tell(new DistributedPubSubMediator.Subscribe(ActorClusterMessageType.RULE_CHAIN_UPDATE.name(), getSelf()), getSelf());
        mediator.tell(new DistributedPubSubMediator.Subscribe(ActorClusterMessageType.RULE_CHAIN_DELETE.name(), getSelf()), getSelf());
        super.preStart();
    }
    
    private void unsubscribe() {
        mediator.tell(new DistributedPubSubMediator.Unsubscribe(ActorClusterMessageType.RULE_CHAIN_INSERT.name(), getSelf()), getSelf());
        mediator.tell(new DistributedPubSubMediator.Unsubscribe(ActorClusterMessageType.RULE_CHAIN_UPDATE.name(), getSelf()), getSelf());
        mediator.tell(new DistributedPubSubMediator.Unsubscribe(ActorClusterMessageType.RULE_CHAIN_DELETE.name(), getSelf()), getSelf());
    }
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BroadCastMessage.class, message -> {
                    switch (message.getMessageType()) {
                        case RULE_CHAIN_INSERT:
                            actorService.addRuleChainsToActorSystem((List<String>)message.getMessage());
                            break;
                        case RULE_CHAIN_UPDATE:
                            actorService.updateRuleChainsInActorSystem((List<String>)message.getMessage());
                            break;
                        case RULE_CHAIN_DELETE:
                            actorService.deleteRuleChainsFromActorSystem((List<MIotRuleEngineRuleChainInitV>)message.getMessage());
                            break;
                        default:
                            hLog.logWhoOccursWhenWarn(this.getClass().getSimpleName(), "a warning", "match broadcast message type", "not found proper type");
                            break;
                    }
                })
                .match(ClusterStatsMessage.class, message -> {
                    if (CLUSTER_LEAVE.equals(message.getMessageType())) {
                        this.unsubscribe();
                    } else {
                        hLog.logWhoOccursWhenError(this.getClass().getSimpleName(), ERROR_STR, "match cluster stats message type", "not found proper type");
                    }
                })
                .match(DistributedPubSubMediator.UnsubscribeAck.class, message -> {
                    hLog.logWhoOccursWhenInfo(this.getClass().getSimpleName(), "a info", String.format(PROCESS_STR, message.getClass()), "receive a unsubscribe ack message");
                    ActorClusterMessageType messageType = ActorClusterMessageType.valueOf(message.unsubscribe().topic());
                    subscribeList.remove(messageType);
                    if (CollectionUtil.isEmpty(subscribeList)) {
                        getSelf().tell(PoisonPill.getInstance(), getSelf());
                    }
                })
                .match(DistributedPubSubMediator.SubscribeAck.class, message -> {
                    String topicStr = message.subscribe().topic();
                    hLog.logWhoOccursWhenInfo(this.getClass().getSimpleName(), "a info", String.format(PROCESS_STR, message.getClass()), String.format("receive a subscribe ack message[%s]", topicStr));
                    ActorClusterMessageType topic = ActorClusterMessageType.valueOf(topicStr);
                    switch(topic) {
                        case RULE_CHAIN_INSERT:
                            subscribeList.add(ActorClusterMessageType.RULE_CHAIN_INSERT);
                            break;
                        case RULE_CHAIN_UPDATE:
                            subscribeList.add(ActorClusterMessageType.RULE_CHAIN_UPDATE);
                            break;
                        case RULE_CHAIN_DELETE:
                            subscribeList.add(ActorClusterMessageType.RULE_CHAIN_DELETE);
                            break;
                        default:
                            hLog.logWhoOccursWhenWarn(this.getClass().getSimpleName(), "a warning", String.format(PROCESS_STR, message.getClass()), "not find proper topic");
                            break;
                    }
                })
                .matchAny(message -> hLog.logWhoOccursWhenError(this.getClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, message.getClass().getSimpleName()), "receive an unknown message: " + message))
                .build();
    }
}
