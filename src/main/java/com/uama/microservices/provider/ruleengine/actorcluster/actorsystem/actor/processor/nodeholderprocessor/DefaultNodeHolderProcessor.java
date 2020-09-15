package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeholderprocessor;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeHolderActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.AbstractActorProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeLifeCycle;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeLifeCycleInfo;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageCarrier;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageType;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;
import com.uama.microservices.provider.ruleengine.support.CloneUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.*;

/**
 * @program: ulink
 * @description: 默认node holder processor (除system node holder actor外的 holder actor processor)
 * @author: liwen
 * @create: 2019-05-10 18:00
 **/
public class DefaultNodeHolderProcessor extends AbstractActorProcessor<NodeHolderActor<DefaultNodeHolderProcessor>> {
    private static final String ACTOR_REF_NOT_EXISTS_STR = "target actor ref not exists, target node id: ";
    private static final String SOURCE_NODE_ID_STR = "source node id: ";
    private static final String CREATE_NODE_ACTOR_STR = "create node actor";
    
    public DefaultNodeHolderProcessor(NodeHolderActor<DefaultNodeHolderProcessor> actor) {
        super(actor);
    }
    
    @Override
    public void dispatchMessage(Object message) {
        if (message instanceof NodeMessageCarrier) {
            NodeMessageCarrier messageCarrier = (NodeMessageCarrier) message;
            NodeMessageType messageType = NodeMessageType.messageClassMap().get(messageCarrier.getMessageClass());
            // if locked, cache message
            if (checkIfNodeIdVLocked(messageCarrier.getTargetNodeIdV()) && !messageType.equals(NodeMessageType.NODE_INIT_MESSAGE)){
                putMessageToCache(messageCarrier);
                return;
            }
            switch (messageType) {
                case NODE_STOP_MESSAGE:
                    this.processNodeStopMessage(messageCarrier);
                    break;
                case NODE_UPDATE_MESSAGE:
                    this.processNodeUpdateMessage(messageCarrier);
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
     * 监管的子actor stop后会收到此消息 从监管map中移除stop的actor
     * @param messageCarrier
     */
    private void processNodeStopMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        NodeId senderNodeIdV = messageCarrier.getSenderNodeIdV();
        if (message instanceof NodeStopMessage) {
            NodeStopMessage nodeStopMessage = (NodeStopMessage) message;
            ActorRef actorRef = nodeStopMessage.getActorRef();
            if (null == actorRef) {
                this.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeStopMessage.getClass().getSimpleName()), "actor ref is null, actor node id: " + senderNodeIdV.getId());
                return;
            }
            // 从监管map中移除actor
            BiMap<ActorRef, NodeId> nodeIdBiMapInv = super.actor.getNodeIdBiMap().inverse();
            if (nodeIdBiMapInv.containsKey(actorRef)) {
                NodeId nodeIdV = nodeIdBiMapInv.remove(actorRef);
                NodeLifeCycleInfo nodeLifeCycleInfo = super.actor.getNodeLifeCycleMap().remove(nodeIdV);
                if (null == nodeLifeCycleInfo) {
                    super.logNodeLifeCycleIsNull(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeStopMessage, nodeIdV);
                }
                this.hLog.logWhoOccursWhenDebug(super.actor.getType().getNodeHolderActorClass().getSimpleName(), DEBUG_STR, String.format(PROCESS_STR, nodeStopMessage.getClass().getSimpleName()), "remove actor ref: " + actorRef.path() + " node id: " + senderNodeIdV.getId());
                
                super.actor.getActorSystemContext().getActorHolderMap().get(ActorTypeMapperEnum.SYSTEM).tell(new NodeMessageCarrier<>(senderNodeIdV, senderNodeIdV, new NodeStopDoneMessage(nodeStopMessage.getNodeInitV())), super.actor.self());
            } else {
                this.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeStopMessage.getClass().getSimpleName()), "find no actor ref: " + actorRef.path() + " node id: " + senderNodeIdV.getId());
            }
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processNodeUpdateMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        NodeId nodeIdV = messageCarrier.getTargetNodeIdV();
        NodeId senderNodeIdV = messageCarrier.getSenderNodeIdV();
        if (message instanceof NodeUpdateMessage) {
            NodeUpdateMessage nodeUpdateMessage = (NodeUpdateMessage) message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeUpdateMessage);
            // lock this chain (new message will forward to itself for futher consuming) (unlock in process init message)
            super.actor.getLockedFirstNodeIdVMap().put(nodeIdV, Lists.newArrayList());
            // delete old chain via sending a delete message
            ActorRef oldActorRef = super.actor.getNodeIdBiMap().get(nodeIdV);
            if (null == oldActorRef) {
                super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeUpdateMessage.getClass().getSimpleName()), ACTOR_REF_NOT_EXISTS_STR + nodeIdV.getId() + ", " + SOURCE_NODE_ID_STR + senderNodeIdV.getId());
                this.tellSenderNodeNotExists(nodeIdV);
                return;
            }
            oldActorRef.tell(nodeUpdateMessage.getNodeDeleteMessage(), super.actor.self());
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processRootRouterUpdateMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        NodeId nodeIdV = messageCarrier.getTargetNodeIdV();
        NodeId senderNodeIdV = messageCarrier.getSenderNodeIdV();
        if (message instanceof RootRouterUpdateMessage) {
            RootRouterUpdateMessage rootRouterUpdateMessage = (RootRouterUpdateMessage) message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), rootRouterUpdateMessage);
            ActorRef actorRef = super.actor.getNodeIdBiMap().get(nodeIdV);
            if (null == actorRef) {
                super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, rootRouterUpdateMessage.getClass().getSimpleName()), ACTOR_REF_NOT_EXISTS_STR + nodeIdV.getId() + ", " + SOURCE_NODE_ID_STR + senderNodeIdV.getId());
                this.tellSenderNodeNotExists(nodeIdV);
                return;
            }
            actorRef.tell(rootRouterUpdateMessage, super.actor.self());
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processNodeDeleteMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        NodeId nodeIdV = messageCarrier.getTargetNodeIdV();
        NodeId senderNodeIdV = messageCarrier.getSenderNodeIdV();
        if (message instanceof NodeDeleteMessage) {
            NodeDeleteMessage nodeDeleteMessage = (NodeDeleteMessage) message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeDeleteMessage);
            ActorRef actorRef = super.actor.getNodeIdBiMap().get(nodeIdV);
            if (null == actorRef) {
                super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeDeleteMessage.getClass().getSimpleName()), ACTOR_REF_NOT_EXISTS_STR + nodeIdV.getId() + ", "+ SOURCE_NODE_ID_STR + senderNodeIdV.getId());
                this.tellSenderNodeNotExists(nodeIdV);
                return;
            }
            actorRef.tell(nodeDeleteMessage, super.actor.self());
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processNodeNotExistsMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        if (message instanceof NodeNotExistsMessage) {
            NodeNotExistsMessage nodeNotExistsMessage = (NodeNotExistsMessage) message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeNotExistsMessage);
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processNodeAlarmMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        NodeId nodeIdV = messageCarrier.getTargetNodeIdV();
        NodeId senderNodeIdV = messageCarrier.getSenderNodeIdV();
        if (message instanceof NodeAlarmMessage) {
            NodeAlarmMessage nodeAlarmMessage = (NodeAlarmMessage) message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeAlarmMessage);
            ActorRef actorRef = super.actor.getNodeIdBiMap().get(nodeIdV);
            if (null == actorRef) {
                super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeAlarmMessage.getClass().getSimpleName()), ACTOR_REF_NOT_EXISTS_STR + nodeIdV.getId() + ", " + SOURCE_NODE_ID_STR + senderNodeIdV.getId());
                this.tellSenderNodeNotExists(nodeIdV);
                return;
            }
            actorRef.tell(nodeAlarmMessage, super.actor.self());
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processNodeTriggerMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        NodeId nodeIdV = messageCarrier.getTargetNodeIdV();
        NodeId senderNodeIdV = messageCarrier.getSenderNodeIdV();
        if (message instanceof NodeTriggerMessage) {
            NodeTriggerMessage nodeTriggerMessage = (NodeTriggerMessage) message;
            super.logReceivedSomeKindOfMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeTriggerMessage);
            ActorRef actorRef = super.actor.getNodeIdBiMap().get(nodeIdV);
            if (null == actorRef) {
                super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeTriggerMessage.getClass().getSimpleName()), ACTOR_REF_NOT_EXISTS_STR + nodeIdV.getId() + ", " + SOURCE_NODE_ID_STR + senderNodeIdV.getId());
                this.tellSenderNodeNotExists(nodeIdV);
                return;
            }
            actorRef.tell(nodeTriggerMessage, super.actor.self());
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private void processNodeDataMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        NodeId nodeIdV = messageCarrier.getTargetNodeIdV();
        NodeId senderNodeIdV = messageCarrier.getSenderNodeIdV();
        if (message instanceof NodeDataMessage) {
            NodeDataMessage nodeDataMessage = (NodeDataMessage) message;
            ActorRef actorRef = super.actor.getNodeIdBiMap().get(nodeIdV);
            if (null == actorRef) {
                super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, String.format(PROCESS_STR, nodeDataMessage.getClass().getSimpleName()), ACTOR_REF_NOT_EXISTS_STR + nodeIdV.getId() + ", " + SOURCE_NODE_ID_STR + senderNodeIdV.getId());
                this.tellSenderNodeNotExists(nodeIdV);
                return;
            }
            actorRef.tell(nodeDataMessage, super.actor.self());
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private boolean checkIfNodeIdVLocked(NodeId targetNodeIdV) {
        boolean locked = false;
        Map<NodeId, List<NodeMessageCarrier>> lockedNodeIdVMap = super.actor.getLockedFirstNodeIdVMap();
        if (null != targetNodeIdV && !lockedNodeIdVMap.isEmpty() && lockedNodeIdVMap.containsKey(targetNodeIdV)) {
            locked = true;
        }
        return locked;
    }
    
    private void tellSenderNodeNotExists(NodeId nodeIdV) {
        super.actor.sender().tell(
                new NodeNotExistsMessage(nodeIdV),
                super.actor.self()
        );
    }
    
    public void processNodeInitMessage(NodeMessageCarrier messageCarrier) {
        Object message = messageCarrier.getMessage();
        NodeId nodeIdV = messageCarrier.getTargetNodeIdV();
        if (message instanceof NodeInitMessage) {
            NodeInitMessage nodeInitMessage = (NodeInitMessage) messageCarrier.getMessage();
            // 需要初始化的node actor是否已经存在
            Boolean needInitNodeActor = checkIfNeedInitNodeActor(nodeIdV, nodeInitMessage);
            if (Boolean.FALSE.equals(needInitNodeActor)) {
                return;
            }
            ActorRef actorRef = createNodeActor(nodeIdV, nodeInitMessage);
            if (null == actorRef) {
                return;
            }
            initNodeActor(actorRef, nodeInitMessage);
            // 释放缓存的message
            if (checkIfNodeIdVLocked(nodeIdV)) {
                releaseCachedMessage(nodeIdV);
            }
        } else {
            super.logReceivedIllegalMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
        }
    }
    
    private Boolean checkIfNeedInitNodeActor(NodeId nodeIdV, NodeInitMessage nodeInitMessage) {
        Boolean checkResult = true;
        Optional<NodeId> nodeIdOpt = super.actor.getNodeIdBiMap().keySet().parallelStream().filter(tarNodeIdV -> tarNodeIdV.equals(nodeIdV)).findAny();
        if (nodeIdOpt.isPresent()) {
            // 已存在 查看该actor初始化时间戳是否与本次需要初始化的时间戳相同
            // 相同则跳过初始化 不同则进行初始化并且替换
            NodeId tarNodeIdV = nodeIdOpt.get();
            NodeLifeCycleInfo nodeLifeCycleInfo = super.actor.getNodeLifeCycleMap().get(tarNodeIdV);
            if (null == nodeLifeCycleInfo) {
                super.logNodeLifeCycleIsNull(super.actor.getType().getNodeHolderActorClass().getSimpleName(), nodeInitMessage, nodeIdV);
            } else if (nodeLifeCycleInfo.getInitTimeStamp() == nodeInitMessage.getInitTimeStamp()) {
                super.hLog.logWhoOccursWhenDebug(super.actor.getType().getNodeHolderActorClass().getSimpleName(), "a debug", String.format("process %s", nodeInitMessage.getClass().getSimpleName()), "node actor already create, nodeId: " + tarNodeIdV);
                checkResult = false;
            }
        }
        return checkResult;
    }
    
    private void initNodeActor(ActorRef actorRef, NodeInitMessage initMessage) {
        actorRef.tell(initMessage, super.actor.self());
    }
    
    private ActorRef createNodeActor(NodeId nodeIdV, NodeInitMessage initMessage) {
        super.hLog.logWhoOccursWhenDebug(super.actor.getType().getNodeHolderActorClass().getSimpleName(), DEBUG_STR, "", "create node actor start, actor id: " + nodeIdV.getId() + ", actor type: " + super.actor.getType().getNodeActorClass().getSimpleName());
        
        // 对初始化的map进行深copy 避免所有node actor用同一个对象造成线程竞争
        Map<String, MIotRuleEngineRuleChainInitV> chainIdMapClone = null;
        List<MIotRuleEngineRuleRelationInitV> toRelationListClone = null;
        List<MIotRuleEngineRuleRelationInitV> fromRelationListClone = null;
        Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMapClone = null;
        Map<String, MIotRuleEngineRuleNodeInitV> ruleNodeIdMap = initMessage.getRuleNodeIdMap();
        try {
            HashMap<String, MIotRuleEngineRuleChainInitV> chainIdMap = (HashMap<String, MIotRuleEngineRuleChainInitV>) initMessage.getRuleChainIdMap();
            ArrayList<MIotRuleEngineRuleRelationInitV> toRelationList = (ArrayList<MIotRuleEngineRuleRelationInitV>) initMessage.getRelationGroupByFromIdMap().get(nodeIdV.getId());
            ArrayList<MIotRuleEngineRuleRelationInitV> fromRelationList = (ArrayList<MIotRuleEngineRuleRelationInitV>) initMessage.getRelationGroupByToIdMap().get(nodeIdV.getId());
            HashMap<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap = (HashMap<String, List<MIotRuleEngineRuleChainInitV>>) initMessage.getChainsGroupByBelongToIdMap();
            if (null != chainIdMap) {
                chainIdMapClone = new ConcurrentHashMap<>(CloneUtils.cloneWithSerialize(chainIdMap));
            }
            if (null != toRelationList) {
                toRelationListClone = Collections.synchronizedList(CloneUtils.cloneWithSerialize(toRelationList));
            }
            if (null != fromRelationList) {
                fromRelationListClone = Collections.synchronizedList(CloneUtils.cloneWithSerialize(fromRelationList));
            }
            if (null != chainsGroupByBelongToIdMap) {
                chainsGroupByBelongToIdMapClone = new ConcurrentHashMap<>(CloneUtils.cloneWithSerialize(chainsGroupByBelongToIdMap));
            }
        } catch (Exception e) {
            super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, CREATE_NODE_ACTOR_STR, "occurs an error when clone maps, exception: " + e.getMessage());
            chainIdMapClone = initMessage.getRuleChainIdMap();
            toRelationListClone = initMessage.getRelationGroupByFromIdMap().get(nodeIdV.getId());
            chainsGroupByBelongToIdMapClone = initMessage.getChainsGroupByBelongToIdMap();
        }
    
        // 创建子actor
        MIotRuleEngineRuleNodeInitV ruleNodeInitV = ruleNodeIdMap.get(nodeIdV.getId());
        if (null == ruleNodeInitV) {
            super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, CREATE_NODE_ACTOR_STR, "rule node[" + nodeIdV.getId() + "] is null, rule node id map: " + ruleNodeIdMap);
            return null;
        }
        ActorRef actorRef = this.createNodeActor(chainIdMapClone, toRelationListClone, fromRelationListClone, ruleNodeInitV, chainsGroupByBelongToIdMapClone);
        
        // 插入或者替换node holder actor监管map
        insertOrUpdateNodeIdBiMap(nodeIdV, actorRef);
        // 插入node声明周期map
        insertOrUpdateNodeLifeCycleMap(nodeIdV, initMessage);
        // 对子actor的stop进行监控
        super.actor.getContext().watchWith(actorRef, new NodeMessageCarrier<>(null, nodeIdV, new NodeStopMessage(actorRef, ruleNodeInitV)));
        
        super.hLog.logWhoOccursWhenDebug(super.actor.getType().getNodeHolderActorClass().getSimpleName(), DEBUG_STR, CREATE_NODE_ACTOR_STR, "create node actor done, actor id: " + nodeIdV.getId());
        return actorRef;
    }
    
    private void insertOrUpdateNodeLifeCycleMap(NodeId nodeIdV, NodeInitMessage initMessage) {
        super.actor.getNodeLifeCycleMap().put(nodeIdV, new NodeLifeCycleInfo(NodeLifeCycle.RUNNING, initMessage.getInitTimeStamp()));
    }
    
    private void insertOrUpdateNodeIdBiMap(NodeId nodeIdV, ActorRef actorRef) {
        ActorRef oldActorRef = super.actor.getNodeIdBiMap().get(nodeIdV);
        if (null != oldActorRef) {
            super.hLog.logWhoOccursWhenError(super.actor.getType().getNodeHolderActorClass().getSimpleName(), ERROR_STR, "insert or update node id bi map", "node[" + nodeIdV.getId() + "] already mapped and will be replaced, ori actor ref: " + oldActorRef.path());
        }
        super.actor.getNodeIdBiMap().put(nodeIdV, actorRef);
    }
    
    private ActorRef createNodeActor(Map<String, MIotRuleEngineRuleChainInitV> chainIdMap,
                                     List<MIotRuleEngineRuleRelationInitV> toRelationList,
                                     List<MIotRuleEngineRuleRelationInitV> fromRelationList,
                                     MIotRuleEngineRuleNodeInitV nodeInitV,
                                     Object... args) {
        if (null == nodeInitV) {
            this.actor.getLogger().error("Actor {} createNodeActor occurred an error, nodeInitV is null", this.actor.getType().getNodeHolderActorClass().getSimpleName());
        }
        if (null == chainIdMap) {
            chainIdMap = Maps.newHashMap();
        }
        if (null == toRelationList) {
            toRelationList = Lists.newArrayList();
        }
        return this.actor.context()
                .actorOf(Props
                        .create(this.actor.getType().getNodeActorClass(),
                                this.actor.getActorSystemContext().getActorHolderMap(),
                                chainIdMap,
                                toRelationList,
                                fromRelationList,
                                nodeInitV,
                                args)
                        .withDispatcher(this.actor.getType().getDispatcherName()));
    }
    
    public void processUnknownMessage(Object message) {
        super.logProcessUnknownMessage(super.actor.getType().getNodeHolderActorClass().getSimpleName(), message);
    }
    
    private void putMessageToCache(NodeMessageCarrier messageCarrier) {
        if (messageCarrier.getMessageClass() == NodeDataMessage.class) {
            super.actor.getLockedFirstNodeIdVMap().get(messageCarrier.getTargetNodeIdV()).add(messageCarrier);
        } else {
            super.actor.getContext().system().deadLetters().tell(messageCarrier, super.actor.self());
        }
    }
    
    private void releaseCachedMessage(NodeId nodeIdV) {
        super.actor.getLockedFirstNodeIdVMap().remove(nodeIdV).stream().filter(Objects::nonNull).forEach(this::dispatchMessage);
    }
}
