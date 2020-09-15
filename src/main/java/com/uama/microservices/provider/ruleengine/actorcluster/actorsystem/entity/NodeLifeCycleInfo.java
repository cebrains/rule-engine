package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity;

import java.util.Date;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-06-13 10:15
 **/
public class NodeLifeCycleInfo {
    
    private final NodeLifeCycle nodeLifeCycle;
    
    private final long initTimeStamp;
    
    public NodeLifeCycleInfo(NodeLifeCycle nodeLifeCycle, long initTimeStamp) {
        this.nodeLifeCycle = nodeLifeCycle;
        this.initTimeStamp = initTimeStamp;
    }
    
    public NodeLifeCycle getNodeLifeCycle() {
        return nodeLifeCycle;
    }
    
    public long getInitTimeStamp() {
        return initTimeStamp;
    }
    
    @Override
    public String toString() {
        return "NodeLifeCycleInfo{" +
                "nodeLifeCycle=" + this.nodeLifeCycle +
                ", initTimeStamp=" + new Date(this.initTimeStamp) +
                '}';
    }
}
