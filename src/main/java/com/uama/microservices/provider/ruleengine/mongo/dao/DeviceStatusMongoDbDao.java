package com.uama.microservices.provider.ruleengine.mongo.dao;

import com.uama.microservices.provider.ruleengine.mongo.entity.DeviceStatus;
import org.springframework.stereotype.Repository;


@Repository
public class DeviceStatusMongoDbDao extends MongoDbDao<DeviceStatus> {
    @Override
    protected Class<DeviceStatus> getEntityClass() {
        return DeviceStatus.class;
    }
}
