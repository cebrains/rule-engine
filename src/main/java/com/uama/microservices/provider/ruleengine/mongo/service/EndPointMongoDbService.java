package com.uama.microservices.provider.ruleengine.mongo.service;

import com.uama.microservices.provider.ruleengine.mongo.dao.DeviceStatusMongoDbDao;
import com.uama.microservices.provider.ruleengine.mongo.dao.EndPointMongoDbDao;
import com.uama.microservices.provider.ruleengine.mongo.entity.DeviceStatus;
import com.uama.microservices.provider.ruleengine.mongo.entity.EndPoint;
import com.uama.microservices.provider.ruleengine.mongo.entity.MongoEntity;
import com.uama.microservices.provider.ruleengine.mongo.model.MongoAsyncSaveResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 描述:
 * mongo
 */
@Service
public class EndPointMongoDbService {
    @Autowired
    private EndPointMongoDbDao endPointMongoDbDao;
    @Autowired
    private DeviceStatusMongoDbDao deviceStatusMongoDbDao;
    
    public MongoAsyncSaveResult asyncSave(MongoEntity entity) {
        if (entity instanceof EndPoint) {
            return endPointMongoDbDao.asyncSave((EndPoint) entity);
        } else if (entity instanceof DeviceStatus) {
            return deviceStatusMongoDbDao.asyncSave((DeviceStatus) entity);
        } else {
            return null;
        }
    }
}
