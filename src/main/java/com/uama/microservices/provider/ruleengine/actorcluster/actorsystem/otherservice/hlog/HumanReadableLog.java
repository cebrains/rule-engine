package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-31 10:59
 **/
public interface HumanReadableLog {
    void logWhoOccursWhenError(String who, String occurs, String when, String error);
    
    void logWhoOccursWhenWarn(String who, String occurs, String when, String warn);
    
    void logWhoOccursWhenDebug(String who, String occurs, String when, String debug);

    void logWhoOccursWhenInfo(String who, String occurs, String when, String info);
}
