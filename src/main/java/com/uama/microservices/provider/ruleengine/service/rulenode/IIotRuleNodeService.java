package com.uama.microservices.provider.ruleengine.service.rulenode;

import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductWarningConfigAddF;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductWarningConfigUpdateF;
import com.uama.microservices.api.ruleengine.model.form.rulerelation.MIotRuleEngineRuleRelationNewF;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleChain;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleNode;

import java.util.List;
import java.util.Set;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-10 17:34
 **/
public interface IIotRuleNodeService {
    
    List<MIotRuleEngineRuleNodeInitV> getRuleNodesForActorSystem(List<String> ruleChainIdList);
    
    MIotRuleEngineRuleRelationNewF addNewProductNode(IotRuleChain ruleChain);
    
    List<IotRuleNode> deleteNodesByRuleChainIdList(List<String> strings);
    
    void updateEndPointFitler(String id, Set<String> endPointList);
    
    IotRuleNode addDataCheckNodeAndRelations(String ruleChainId, MIotRuleEngineProductWarningConfigAddF mIotRuleEngineF);
    
    List<String> updateDataCheck(MIotRuleEngineProductWarningConfigUpdateF mIotRuleEngineF);
    
    IotRuleNode selectRuleNodeByPrimaryKey(String id);
    
    void deleteRuleNodeByPrimaryKey(String id);
}
