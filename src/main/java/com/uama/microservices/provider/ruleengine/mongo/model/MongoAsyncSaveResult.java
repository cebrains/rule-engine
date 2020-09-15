package com.uama.microservices.provider.ruleengine.mongo.model;

import com.uama.microservices.provider.ruleengine.mongo.enums.MongoAsyncSaveResultEnum;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-11-01 16:35
 **/
public class MongoAsyncSaveResult {
    
    private MongoAsyncSaveResultEnum result;
    
    public MongoAsyncSaveResultEnum getResult() {
        return result;
    }
    
    public void setResult(MongoAsyncSaveResultEnum result) {
        this.result = result;
    }
}
