package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import com.alibaba.fastjson.JSONArray;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;

import java.util.Date;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-15 19:18
 **/
public class NodeAlarmMessage implements NodeMessage {
    
    // 触发node id
    private final NodeId triggerNodeIdV;
    
    // 触发属性
    private final JSONArray triggerFields;
    
    // 是否报警
    private final boolean alert;
    
    // 报警时间
    private final Date notificationTime;
    
    // 触发报警原数据
    private final NodeDataMessage nodeDataMessage;
    
    public NodeAlarmMessage(NodeId triggerNodeIdV, JSONArray triggerFields, boolean alert, Date notificationTime, NodeDataMessage nodeDataMessage) {
        this.triggerNodeIdV = triggerNodeIdV;
        this.triggerFields = triggerFields;
        this.alert = alert;
        this.notificationTime = notificationTime;
        this.nodeDataMessage = nodeDataMessage;
    }
    
    public NodeId getTriggerNodeIdV() {
        return triggerNodeIdV;
    }
    
    public JSONArray getTriggerFields() {
        return triggerFields;
    }
    
    public NodeDataMessage getNodeDataMessage() {
        return nodeDataMessage;
    }
    
    public boolean isAlert() {
        return alert;
    }
    
    public Date getNotificationTime() {
        return notificationTime;
    }
    
    @Override
    public String toString() {
        return "NodeAlarmMessage{" +
                "triggerNodeIdV=" + this.triggerNodeIdV +
                ", notificationTime=" + this.notificationTime +
                ", nodeDataMessage=" + this.nodeDataMessage +
                '}';
    }
}
