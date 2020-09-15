package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-31 11:00
 **/
public abstract class AbstractHumanReadableLog implements HumanReadableLog {
    public static final String INFO_STR = "a info";
    public static final String ERROR_STR = "an error";
    public static final String DEBUG_STR = "a debug";
    public static final String PROCESS_STR = "process %s";
    
    @Override
    public void logWhoOccursWhenError(String who, String occurs, String when, String error) {
        this.doLogWhoOccursWhenError(who, occurs, when, error);
    }

    @Override
    public void logWhoOccursWhenWarn(String who, String occurs, String when, String warn) {
        this.doLogWhoOccursWhenWarn(who, occurs, when, warn);
    }

    @Override
    public void logWhoOccursWhenDebug(String who, String occurs, String when, String debug) {
        this.doLogWhoOccursWhenDebug(who, occurs, when, debug);
    }

    @Override
    public void logWhoOccursWhenInfo(String who, String occurs, String when, String info) {
        this.doLogWhoOccursWhenInfo(who, occurs, when, info);
    }

    protected abstract void doLogWhoOccursWhenError(String who, String occurs, String when, String error);

    protected abstract void doLogWhoOccursWhenWarn(String who, String occurs, String when, String warn);

    protected abstract void doLogWhoOccursWhenDebug(String who, String occurs, String when, String debug);

    protected abstract void doLogWhoOccursWhenInfo(String who, String occurs, String when, String info);
}
