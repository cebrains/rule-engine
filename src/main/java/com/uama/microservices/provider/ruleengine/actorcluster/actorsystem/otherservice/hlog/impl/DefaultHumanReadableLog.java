package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.impl;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-31 11:03
 **/
@Slf4j
@Component
public class DefaultHumanReadableLog extends AbstractHumanReadableLog {
    
    private static final String BASE_TEMPLTE = "{} occurs {} when {}, ";
    
    @Override
    protected void doLogWhoOccursWhenError(String who, String occurs, String when, String error) {
        log.error(DefaultHumanReadableLog.BASE_TEMPLTE + "error: " + error, who, occurs, when);
    }
    
    @Override
    protected void doLogWhoOccursWhenWarn(String who, String occurs, String when, String warn) {
        log.warn(DefaultHumanReadableLog.BASE_TEMPLTE + "warn: " + warn, who, occurs, when);
    }
    
    @Override
    protected void doLogWhoOccursWhenDebug(String who, String occurs, String when, String debug) {
        log.debug(DefaultHumanReadableLog.BASE_TEMPLTE + "debug: " + debug, who, occurs, when);
    }

    @Override
    protected void doLogWhoOccursWhenInfo(String who, String occurs, String when, String info) {
        log.info(DefaultHumanReadableLog.BASE_TEMPLTE + "info: " + info, who, occurs, when);
    }
}



