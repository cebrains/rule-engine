package com.uama.microservices.provider.ruleengine.mongo.dao;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.NodeMessage;
import org.springframework.stereotype.Repository;


@Repository
public class DeadLetterMongoDbDao extends MongoDbDao<NodeMessage> {
    @Override
    protected Class<NodeMessage> getEntityClass() {
        return NodeMessage.class;
    }
}
