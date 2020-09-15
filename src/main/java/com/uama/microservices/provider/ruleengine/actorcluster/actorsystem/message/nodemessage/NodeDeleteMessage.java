package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage;

import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-22 16:48
 **/
public class NodeDeleteMessage implements NodeMessage {
    
    // 删除的链路信息
    private final MIotRuleEngineRuleChainInitV ruleChainV;
    
    // 是否为删除
    private final boolean deleteFlag;
    
    // 是否为根链
    private final boolean root;
    
    // 删除标记时间戳
    private final long deleteTimeStamp;
    
    public NodeDeleteMessage(MIotRuleEngineRuleChainInitV ruleChainV, boolean deleteFlag, boolean root, long deleteTimeStamp) {
        this.ruleChainV = ruleChainV;
        this.deleteFlag = deleteFlag;
        this.root = root;
        this.deleteTimeStamp = deleteTimeStamp;
    }
    
    public MIotRuleEngineRuleChainInitV getRuleChainV() {
        return ruleChainV;
    }
    
    public boolean isDeleteFlag() {
        return deleteFlag;
    }
    
    public boolean isRoot() {
        return root;
    }
    
    public long getDeleteTimeStamp() {
        return deleteTimeStamp;
    }
    
    @Override
    public String toString() {
        return "NodeDeleteMessage{" +
                "ruleChainV=" + this.ruleChainV +
                ", deleteFlag=" + this.deleteFlag +
                ", root=" + this.root +
                ", deleteTimeStamp=" + this.deleteTimeStamp +
                '}';
    }
}
