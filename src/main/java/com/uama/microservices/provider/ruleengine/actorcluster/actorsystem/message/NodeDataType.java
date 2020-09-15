package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ConstantFields;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-15 10:12
 **/
public enum NodeDataType {
    DEVICE_DATA_END_POINT(ConstantFields.DEVICE_DATA, ConstantFields.END_POINT),
    DEVICE_DATA_DEVICE_STATUS(ConstantFields.DEVICE_DATA, ConstantFields.DEVICE_STATUS)
    ;
    
    private String dataTypeNameI;
    private String dataTypeNameII;
    
    NodeDataType(String dataTypeNameI, String dataTypeNameII) {
        this.dataTypeNameI = dataTypeNameI;
        this.dataTypeNameII = dataTypeNameII;
    }
    
    public String getDataTypeNameI() {
        return dataTypeNameI;
    }
    
    public String getDataTypeNameII() {
        return dataTypeNameII;
    }
}
