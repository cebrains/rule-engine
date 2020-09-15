package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor;

import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor.SaveDBNodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice.DefaultActorService;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;
import com.uama.microservices.provider.ruleengine.mongo.entity.DeviceStatus;
import com.uama.microservices.provider.ruleengine.mongo.entity.EndPoint;
import com.uama.microservices.provider.ruleengine.mongo.enums.MongoAsyncSaveResultEnum;
import com.uama.microservices.provider.ruleengine.mongo.model.MongoAsyncSaveResult;

import java.util.List;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.DEBUG_STR;
import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.ERROR_STR;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 10:32
 **/
public class DefaultSaveDBNodeProcessor extends BaseNodeProcessor<SaveDBNodeActor> {
    private static final String SAVE_TO_DB_STR = "save to database";
    
    public DefaultSaveDBNodeProcessor(SaveDBNodeActor actor) {
        super(actor);
    }
    
    public void processRootRouterUpdateMessage(RootRouterUpdateMessage message) {
        if (!super.checkIfNull(message)) {
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                super.sendToNextNodeHolderActor(super.actor.getNodeIdV(), message, toRelationList, super.actor.getChainIdMap(), super.actor.getActorHolderMap(), false);
            }
        }
    }
    
    public void processNodeDeleteMessage(NodeDeleteMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            // 如果删除时间标记已被标记 并且等于本次的删除标记 则认为删除message已经下发到下层node 不需要再次下发
            if (message.getDeleteTimeStamp() != super.actor.getDeleteTimeStamp() && CollectionUtil.isNotEmpty(toRelationList)) {
                super.actor.setDeleteTimeStamp(message.getDeleteTimeStamp());
                super.sendToNextNodeHolderActor(super.actor.getNodeIdV(), message, toRelationList, super.actor.getChainIdMap(), super.actor.getActorHolderMap(), false);
            }
            // 所有的上层delete message 都到达后 才会停止此node actor
            if (message.isDeleteFlag()) {
                List<MIotRuleEngineRuleRelationInitV> fromRelationList = super.actor.getFromRelationList();
                fromRelationList.remove(0);
                if (CollectionUtil.isEmpty(fromRelationList)) {
                    super.stopActor(super.actor.self());
                }
            }
        }
    }
    
    public void processNodeNotExistsMessage(NodeNotExistsMessage message) {
        if (!super.checkIfNull(message)) {
            super.logReceivedSomeKindOfMessage(super.logWho, message);
            NodeId nodeIdV = message.getNodeIdV();
            // 删除下层关系 toId有可能是node id 或 rule chain id
            super.deleteToRelationByToNodeId(super.actor.getToRelationList(), nodeIdV, super.getRuleChainIdIfFirstNode(super.actor.getChainIdMap(), nodeIdV));
        }
    }
    
    public void processNodeAlarmMessage(NodeAlarmMessage message) {
        if (!super.checkIfNull(message)) {
            this.processNodeDataMessage(message.getNodeDataMessage());
        }
    }
    
    public void processNodeTriggerMessage(NodeTriggerMessage message) {
        if (!super.checkIfNull(message)) {
            this.processNodeDataMessage(message.getNodeDataMessage());
        }
    }
    
    public void processNodeDataMessage(NodeDataMessage message) {
        if (!super.checkIfNull(message)) {
            saveToDatabase(message);
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                super.sendToNextNodeHolderActor(super.actor.getNodeIdV(), message, toRelationList, super.actor.getChainIdMap(), super.actor.getActorHolderMap(), true);
            }
        }
    }
    
    private void saveToDatabase(NodeDataMessage message) {
        MongoAsyncSaveResult asyncSaveResult;
        switch (message.getDataType()) {
            case DEVICE_DATA_END_POINT:
                EndPoint endPoint = new EndPoint();
                endPoint.setDeviceId(message.getDeviceId());
                endPoint.setProductId(message.getProductId());
                endPoint.setEndPointJStr(message.getEndPoints());
                endPoint.setDataTime(message.getDataTime());
                endPoint.setDataTimeLong(message.getDataTime().getTime());
                endPoint.setDataType(message.getDataType().getDataTypeNameII());
                super.hLog.logWhoOccursWhenDebug(super.logWho, DEBUG_STR, SAVE_TO_DB_STR, "save end point: " + endPoint);
                asyncSaveResult = DefaultActorService.defultActorDependenceService().getEndPointMongoDbService().asyncSave(endPoint);
                break;
            case DEVICE_DATA_DEVICE_STATUS:
                DeviceStatus deviceStatus = new DeviceStatus();
                deviceStatus.setDeviceId(message.getDeviceId());
                deviceStatus.setProductId(message.getProductId());
                deviceStatus.setEndPointJStr(message.getEndPoints());
                deviceStatus.setDataTime(message.getDataTime());
                deviceStatus.setDataTimeLong(message.getDataTime().getTime());
                deviceStatus.setDataType(message.getDataType().getDataTypeNameII());
                super.hLog.logWhoOccursWhenDebug(super.logWho, DEBUG_STR, SAVE_TO_DB_STR, "save deivce status: " + deviceStatus);
                asyncSaveResult = DefaultActorService.defultActorDependenceService().getEndPointMongoDbService().asyncSave(deviceStatus);
                break;
            default:
                asyncSaveResult = null;
        }
        if (null != asyncSaveResult) {
            MongoAsyncSaveResultEnum resultEnum = asyncSaveResult.getResult();
            if (MongoAsyncSaveResultEnum.ASYNC_SAVE_QUEUE_FULL.equals(resultEnum)) {
                super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, SAVE_TO_DB_STR, "mongo async save queue is full, message: " + message.toString());
            } else if (MongoAsyncSaveResultEnum.ASYNC_SAVE_QUEUE_HIGH_LEVEL.equals(resultEnum)) {
                super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, SAVE_TO_DB_STR, "mongo async save queue remaining capacity is low");
            } else if (MongoAsyncSaveResultEnum.ASYNC_SAVE_FAIL.equals(resultEnum)) {
                super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, SAVE_TO_DB_STR, "mongo async save fail, message: " + message.toString());
            }
        }
    }
    
    public void processNodeInitMessage(NodeInitMessage message) {
        if (!super.checkIfNull(message)) {
            List<MIotRuleEngineRuleRelationInitV> toRelationList = super.actor.getToRelationList();
            if (CollectionUtil.isNotEmpty(toRelationList)) {
                super.sendToNextNodeHolderActor(
                        super.actor.getNodeIdV(),
                        message,
                        toRelationList,
                        super.actor.getChainIdMap(),
                        super.actor.getActorHolderMap(),
                        false
                );
            }
        }
    }
}
