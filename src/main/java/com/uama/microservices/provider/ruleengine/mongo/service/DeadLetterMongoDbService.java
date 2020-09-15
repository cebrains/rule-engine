package com.uama.microservices.provider.ruleengine.mongo.service;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.NodeMessage;
import com.uama.microservices.provider.ruleengine.mongo.dao.DeadLetterMongoDbDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-11 11:32
 **/
@Service
public class DeadLetterMongoDbService {
    @Autowired
    private DeadLetterMongoDbDao deadLetterMongoDbDao;
    
    public void asyncSave(NodeMessage message) {
        deadLetterMongoDbDao.asyncSave(message);
    }
}
