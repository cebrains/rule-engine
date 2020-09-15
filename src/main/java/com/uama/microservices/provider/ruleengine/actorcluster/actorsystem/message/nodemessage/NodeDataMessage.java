package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import com.alibaba.fastjson.annotation.JSONField;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeDataType;

import java.util.Date;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-15 10:10
 **/
public class NodeDataMessage implements NodeMessage {
    
    private final String deviceId;
    
    private final String productId;
    
    private final String endPoints;
    
    private final Date dataTime;
    
    @JSONField(serialize = false)
    private final NodeDataType dataType;
    
    public NodeDataMessage(String deviceId, String productId, String endPoints, NodeDataType dataType, Date dataTime) {
        this.deviceId = deviceId;
        this.productId = productId;
        this.endPoints = endPoints;
        this.dataType = dataType;
        this.dataTime = dataTime;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public String getEndPoints() {
        return endPoints;
    }
    
    public NodeDataType getDataType() {
        return dataType;
    }
    
    public Date getDataTime() {
        return dataTime;
    }
    
    @Override
    public String toString() {
        return "NodeDataMessage{" +
                "deviceId='" + deviceId + '\'' +
                ", productId='" + productId + '\'' +
                ", endPoints='" + endPoints + '\'' +
                ", dataTime=" + dataTime +
                ", dataType=" + dataType +
                '}';
    }
}
