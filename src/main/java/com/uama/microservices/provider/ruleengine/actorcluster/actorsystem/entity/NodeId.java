package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity;

import com.google.common.base.Objects;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 21:23
 **/
public class NodeId {
    private String id;
    
    public NodeId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeId nodeId = (NodeId) o;
        return Objects.equal(id, nodeId.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "NodeId{" +
                "id='" + this.id + '\'' +
                '}';
    }
}
