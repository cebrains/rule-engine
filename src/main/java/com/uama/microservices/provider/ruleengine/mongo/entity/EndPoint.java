package com.uama.microservices.provider.ruleengine.mongo.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-15 15:46
 **/
public class EndPoint extends MongoEntity {
    
    private String deviceId;
    
    private String productId;
    
    private String endPointJStr;
    
    private Date dataTime;
    
    private Long dataTimeLong;
    
    private String dataType;
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getEndPointJStr() {
        return endPointJStr;
    }
    
    public void setEndPointJStr(String endPointJStr) {
        this.endPointJStr = endPointJStr;
    }
    
    public Date getDataTime() {
        return dataTime;
    }
    
    public void setDataTime(Date dataTime) {
        this.dataTime = dataTime;
    }
    
    public Long getDataTimeLong() {
        return dataTimeLong;
    }
    
    public void setDataTimeLong(Long dataTimeLong) {
        this.dataTimeLong = dataTimeLong;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
