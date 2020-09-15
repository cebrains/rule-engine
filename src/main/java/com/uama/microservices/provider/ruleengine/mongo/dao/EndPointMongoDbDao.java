package com.uama.microservices.provider.ruleengine.mongo.dao;

import com.uama.microservices.provider.ruleengine.mongo.entity.EndPoint;
import org.springframework.stereotype.Repository;


@Repository
public class EndPointMongoDbDao extends MongoDbDao<EndPoint> {
    @Override
    protected Class<EndPoint> getEntityClass() {
        return EndPoint.class;
    }
}
