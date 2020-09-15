package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice.exception;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-11-19 15:11
 **/
public class FunctionInvokeException extends RuntimeException {
    
    public FunctionInvokeException() {
        super();
    }
    
    public FunctionInvokeException(String message) {
        super(message);
    }
}
