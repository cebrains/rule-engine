package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeholderprocessor;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.enums.IotRuleChainRootEnum;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeHolderActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeholderactor.SystemNodeHolderActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.AbstractActorProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.SystemRuleChainContext;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageCarrier;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageType;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.*;

/**
 * @program: ulink
 * @description: system node holder actor专用processor
 * @author: liwen
 * @create: 2019-05-14 20:51
 **/
public class DefaultSystemNodeHolderProcessor extends AbstractActorProcessor<NodeHolderActor<DefaultSystemNodeHolderProcessor>> {
    
    public DefaultSystemNodeHolderProcessor(NodeHolderActor<DefaultSystemNodeHolderProcessor> actor) {
        super(actor);
    }
    
    @Override
    public void dispatchMessage(Object message) {
        if (message instanceof NodeMessageCarrier) {
            NodeMessageCarrier messageCarrier = (NodeMessageCarrier)message;
            NodeMessageType messageType = NodeMessageType.messageClassMap().get(messageCarrier.getMessageClass());
            switch(messageType) {
                case NODE_STOP_DONE_MESSAGE:
                    this.processNodeStopDoneMessage(messageCarrier);
                    break;
                case NODE_UPDATE_MESSAGE:
                    this.processNodeUpdateMessage(messageCarrier);
                    break;
                case NODE_STOP_MESSAGE:
                    this.processNodeStopMessage(messageCarrier);
                    break;
                case ROOT_ROUTER_UPDATE_MESSAGE:
                    this.processRootRouterUpdateMessage(messageCarrier);
                    break;
                case NODE_DELETE_MESSAGE:
                    this.processNodeDeleteMessage(messageCarrier);
                    break;
                case NODE_NOT_EXISTS_MESSAGE:
                    this.processNodeNotExistsMessage(messageCarrier);
                    break;
                case NODE_ALARM_MESSAGE:
                    this.processNodeAlarmMessage(messageCarrier);
                    break;
                case NODE_TRIGGER_MESSAGE:
                    this.processNodeTriggerMessage(messageCarrier);
                    break;
                case NODE_DATA_MESSAGE:
                    this.processNodeDataMessage(messageCarrier);
                    break;
                case NODE_INIT_MESSAGE:
                    this.processNodeInitMessage(messageCarrier);
                    break;
                default:
                    this.processUnknownMessage(messageCarrier.getMessage());
                    break;
            }
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    /**
     * 用于链路更新 更新的链路会先把每个node stop 每个node stop后会由其监管holder actor给system 发送stop done message
     * @param messageCarrier
     */
    private void processNodeStopDoneMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        if (message instanceof NodeStopDoneMessage) {
            NodeStopDoneMessage nodeStopDoneMessage = (NodeStopDoneMessage)message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeStopDoneMessage);
            
            MIotRuleEngineRuleNodeInitV nodeInitV = nodeStopDoneMessage.getNodeInitV();
            String ruleChainId = nodeInitV.getRuleChainId();
            String nodeId = nodeInitV.getId();
            SystemRuleChainContext ruleChainContext = SystemNodeHolderActor.ruleChainIdContextMap().get(ruleChainId);
            if (null == ruleChainContext) {
                super.logRuleChainContextNotExists(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeStopDoneMessage, ruleChainId);
                return;
            }
            // system actor holder 中缓存的每个链路的context 如果链路中的节点都stop done则此集合为empty, 说明链路全部node已经stop 可以进行初始化
            List<MIotRuleEngineRuleNodeInitV> nodeInitVList = ruleChainContext.getNodeInitVList();
            nodeInitVList.removeIf(endNodeInitV -> endNodeInitV.getId().equals(nodeId));
            if (CollectionUtil.isEmpty(nodeInitVList)) {
                // 进行初始化
                NodeInitMessage nodeInitMessage = ruleChainContext.getUpdateInitMessage();
                if (null == nodeInitMessage) {
                    SystemNodeHolderActor.ruleChainIdContextMap().remove(ruleChainId);
                } else {
                    Map<String, MIotRuleEngineRuleNodeInitV> ruleNodeIdMap = nodeInitMessage.getRuleNodeIdMap();
                    initRuleChainContext(nodeInitMessage.getRelationGroupByFromIdMap(), ruleChainId, ruleNodeIdMap);
                    SystemNodeHolderActor.ruleChainIdContextMap().get(ruleChainId).getStartNodeInitVList().parallelStream().forEach(startNodeInitV -> {
                        String startNodeId = startNodeInitV.getId();
                        ActorTypeMapperEnum startNodeActorTypeEnum = ActorTypeMapperEnum.valueOf(ruleNodeIdMap.get(startNodeId).getNodeType());
                        super.actor.getActorSystemContext().getActorHolderMap().get(startNodeActorTypeEnum)
                                .tell(new NodeMessageCarrier<>(new NodeId(startNodeId),
                                                null,
                                                nodeInitMessage),
                                        ActorRef.noSender());
                    });
                    ruleChainContext.setUpdateInitMessage(null);
                }
            }
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processNodeUpdateMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        if (message instanceof NodeUpdateMessage) {
            NodeUpdateMessage nodeUpdateMessage = (NodeUpdateMessage)message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeUpdateMessage);
            MIotRuleEngineRuleChainInitV ruleChainInitV = nodeUpdateMessage.getNodeDeleteMessage().getRuleChainV();
            String ruleChainId = ruleChainInitV.getId();
            SystemRuleChainContext ruleChainContext = SystemNodeHolderActor.ruleChainIdContextMap().get(ruleChainId);
            if (null == ruleChainContext) {
                super.logRuleChainContextNotExists(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeUpdateMessage, ruleChainId);
            } else {
                if (null != ruleChainContext.getUpdateInitMessage()) {
                    // 上次更新还未完成
                    super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeUpdateMessage.getClass().getSimpleName()), "rule chain[" + ruleChainId + "] context is updating, update init message: " + ruleChainContext.getUpdateInitMessage());
                    ruleChainContext.setUpdateInitMessage(nodeUpdateMessage.getNodeInitMessage());
                    return;
                }
                // 设置需要更新的message 环境
                ruleChainContext.setUpdateInitMessage(nodeUpdateMessage.getNodeInitMessage());
                String firstNodeId = ruleChainInitV.getFirstNodeId();
                // 给需要更新的链路所有头node发送message first node 会发送update message 其余的发送delete message
                ruleChainContext.getStartNodeInitVList().parallelStream().forEach(nodeInitV -> {
                    ActorRef targetRef = super.actor.getActorSystemContext().getActorHolderMap().get(ActorTypeMapperEnum.valueOf(nodeInitV.getNodeType()));
                    if (nodeInitV.getId().equals(firstNodeId)) {
                        targetRef.tell(messageCarrier, super.actor.self());
                    } else {
                        targetRef.tell(new NodeMessageCarrier<>(new NodeId(nodeInitV.getId()), null, nodeUpdateMessage.getNodeDeleteMessage()), super.actor.self());
                    }
                });
            }
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    /**
     * 理论上目前还不会收到此类message
     * @param messageCarrier
     */
    private void processNodeStopMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        if (message instanceof NodeStopMessage) {
            NodeStopMessage nodeStopMessage = (NodeStopMessage) message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeStopMessage);
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    /**
     * 更新root 的 router 用于新链路加入可以路由到新的产品链路
     * @param messageCarrier
     */
    private void processRootRouterUpdateMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        if (message instanceof RootRouterUpdateMessage) {
            RootRouterUpdateMessage rootRouterUpdateMessage = (RootRouterUpdateMessage)message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), rootRouterUpdateMessage);
            Map<NodeId, ActorRef> nodeIdBiMap = super.actor.getNodeIdBiMap();
            if (!nodeIdBiMap.isEmpty()) {
                // 给所有的根链路发送消息(目前只有一个root chain)
                this.dispatchToRoots(rootRouterUpdateMessage);
            }
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    /**
     * 删除某产品时会发送此消息到system holder actor 由它发送到所有根链路 然后更新根链路的router 并且在router中给链路下发delete message
     * 这里只会接收到不为root 并且isDelete为false消息 在router下发的时候会将isDelete改为true执行删除
     * @param messageCarrier
     */
    private void processNodeDeleteMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        if (message instanceof NodeDeleteMessage) {
            NodeDeleteMessage nodeDeleteMessage = (NodeDeleteMessage)message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeDeleteMessage);
            if ((nodeDeleteMessage.isDeleteFlag() && !nodeDeleteMessage.isRoot())
                    || (!nodeDeleteMessage.isDeleteFlag() && nodeDeleteMessage.isRoot())) {
                super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeDeleteMessage.getClass().getSimpleName()), "receive an invalid node delete message, cause not expected, message: " + nodeDeleteMessage);
                return;
            }
            Map<NodeId, ActorRef> nodeIdBiMap = super.actor.getNodeIdBiMap();
            if (nodeDeleteMessage.isRoot() && nodeDeleteMessage.isDeleteFlag()) {
                super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeDeleteMessage.getClass().getSimpleName()), "receive a node delete message to delete root chain, but not permitted, message: " + nodeDeleteMessage);
                return;
            }
            if (!nodeDeleteMessage.isRoot() && !nodeIdBiMap.isEmpty()) {
                this.dispatchToRoots(nodeDeleteMessage);
            }
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processNodeNotExistsMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        if (message instanceof NodeNotExistsMessage) {
            NodeNotExistsMessage nodeNotExistsMessage = (NodeNotExistsMessage)message;
            super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeNotExistsMessage.getClass().getSimpleName()), "receive message, but not expected, message: " + nodeNotExistsMessage);
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processNodeAlarmMessage(NodeMessageCarrier messageCarrier) {
        this.processNodeDataMessage(messageCarrier);
    }
    
    private void processNodeTriggerMessage(NodeMessageCarrier messageCarrier) {
        this.processNodeDataMessage(messageCarrier);
    }
    
    private void processNodeDataMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        if (message instanceof NodeDataMessage) {
            NodeDataMessage nodeDataMessage = (NodeDataMessage) message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeDataMessage);
            Map<NodeId, ActorRef> nodeIdBiMap = super.actor.getNodeIdBiMap();
            if (!nodeIdBiMap.isEmpty()) {
                this.dispatchToRoots(nodeDataMessage);
            }
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    /**
     * 初始化子actor 为各root chain的first node的镜像
     * @param messageCarrier
     */
    public void processNodeInitMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        NodeId nodeIdV = messageCarrier.getTargetNodeIdV();
        if (message instanceof NodeInitMessage) {
            NodeInitMessage nodeInitMessage = (NodeInitMessage)message;
            Map<String, MIotRuleEngineRuleNodeInitV> ruleNodeIdMap = nodeInitMessage.getRuleNodeIdMap();
            MIotRuleEngineRuleNodeInitV ruleNodeInitV = ruleNodeIdMap.get(nodeIdV.getId());
            MIotRuleEngineRuleChainInitV ruleChainInitV = nodeInitMessage.getRuleChainIdMap().get(ruleNodeInitV.getRuleChainId());
            if (IotRuleChainRootEnum.ROOT.getCode().equals(ruleChainInitV.getRoot())) {
                createNodeActor(nodeIdV, ruleNodeInitV);
            }
            // 初始化链路context
            initRuleChainContext(nodeInitMessage.getRelationGroupByFromIdMap(), ruleChainInitV.getId(), ruleNodeIdMap);
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void initRuleChainContext(Map<String, List<MIotRuleEngineRuleRelationInitV>> relationGroupByFromIdMap, String ruleChainId, Map<String, MIotRuleEngineRuleNodeInitV> ruleNodeIdMap) {
        SystemRuleChainContext ruleChainContext = new SystemRuleChainContext();
        ruleChainContext.setRuleChainId(ruleChainId);
        List<MIotRuleEngineRuleNodeInitV> startRuleNodeInitVList = relationGroupByFromIdMap.get(ruleChainId).parallelStream().map(ruleRelationInitV -> ruleNodeIdMap.get(ruleRelationInitV.getToId())).collect(Collectors.toList());
        ruleChainContext.setStartNodeInitVList(Collections.synchronizedList(startRuleNodeInitVList));
        ruleChainContext.setNodeInitVList(Collections.synchronizedList(ruleNodeIdMap.values().parallelStream().collect(Collectors.toList())));
        SystemNodeHolderActor.ruleChainIdContextMap().put(ruleChainId, ruleChainContext);
    }
    
    private void createNodeActor(NodeId nodeIdV, MIotRuleEngineRuleNodeInitV ruleNodeInitV) {
        super.hLog.logWhoOccursWhenDebug(super.actor.getType().getNodeHolderActorClass().getSimpleName(), DEBUG_STR, "create node actor", "create node actor start, actor id: " + nodeIdV.getId() + ", actor type: " + super.actor.getType().getNodeActorClass().getSimpleName());
        
        ActorRef actorRef = super.actor.context().actorOf(Props
                .create(super.actor.getType().getNodeActorClass(),
                        super.actor.getActorSystemContext().getActorHolderMap(),
                        ruleNodeInitV
                ).withDispatcher(super.actor.getType().getDispatcherName()));
        
        super.actor.getNodeIdBiMap().put(nodeIdV, actorRef);
        super.actor.getContext().watchWith(actorRef, new NodeMessageCarrier<>(null, nodeIdV, new NodeStopMessage(actorRef, ruleNodeInitV)));
        
        super.hLog.logWhoOccursWhenDebug(super.actor.getType().getNodeHolderActorClass().getSimpleName(), DEBUG_STR, "create node actor", "create node actor done, actor id: " + nodeIdV.getId());
    }
    
    public void processUnknownMessage(Object message) {
        super.logProcessUnknownMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
    }
    
    private void dispatchToRoots(NodeMessage messageCarrier) {
        super.actor.getNodeIdBiMap().values()
                .parallelStream()
                .filter(Objects::nonNull)
                .forEach(actorRef -> actorRef.tell(messageCarrier, super.actor.self()));
    }
}
