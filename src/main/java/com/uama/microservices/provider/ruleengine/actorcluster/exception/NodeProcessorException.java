package com.uama.microservices.provider.ruleengine.actorcluster.exception;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-11-20 09:24
 **/
public class NodeProcessorException extends RuntimeException {
    public NodeProcessorException(String message) {
        super(message);
    }
}
