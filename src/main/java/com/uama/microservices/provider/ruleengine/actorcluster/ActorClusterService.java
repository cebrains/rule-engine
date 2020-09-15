package com.uama.microservices.provider.ruleengine.actorcluster;

import com.uama.microservices.provider.ruleengine.actorcluster.message.BroadCastMessage;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-09-02 17:31
 **/
public interface ActorClusterService {
    void broadCast(BroadCastMessage message);
}
