package com.uama.microservices.provider.ruleengine.stream.handler;

import com.uama.microservices.iot.stream.model.device.MIotRuleEngineDeviceDataStreamF;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice.ActorService;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeDataType;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.NodeDataMessage;
import com.uama.microservices.provider.ruleengine.stream.channel.IotUlinkRuleEngineStreamInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
public class UlinkActorHandler {
    
    @Autowired
    private ActorService actorService;
    
    @StreamListener(target = IotUlinkRuleEngineStreamInput.ACTOR_HANDLER_INPUT, condition = "headers['eventType']=='RECEIVE_DATA_FOR_RULE_ENGINE'")
    public void receiveDataForRuleEngine(MIotRuleEngineDeviceDataStreamF mStreamF) {
        NodeDataMessage nodeDataMessage = convertToActorMessage(mStreamF);
        actorService.input(nodeDataMessage);
    }
    
    private NodeDataMessage convertToActorMessage(MIotRuleEngineDeviceDataStreamF mStreamF) {
        NodeDataType dataType = NodeDataType.valueOf(mStreamF.getDataType());
        return new NodeDataMessage(mStreamF.getDeviceId(), mStreamF.getProductId(), mStreamF.getEndPointJStr(), dataType, mStreamF.getDataTime());
    }
}
