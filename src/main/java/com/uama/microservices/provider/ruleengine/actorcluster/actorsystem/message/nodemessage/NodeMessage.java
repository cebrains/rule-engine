package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-15 11:51
 **/
@Document(collection = "deadLetter")
public interface NodeMessage {
}
