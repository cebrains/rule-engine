package com.uama.microservices.provider.ruleengine.service.rulerelation;

import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductWarningConfigDeleteF;
import com.uama.microservices.api.ruleengine.model.form.rulerelation.MIotRuleEngineRuleRelationNewF;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleChain;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleNode;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleRelation;

import java.util.List;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-10 17:35
 **/
public interface IIotRuleRelationService {
    
    List<MIotRuleEngineRuleRelationInitV> getRuleRelationsForActorSystem(List<String> ruleChainIdList);
    
    MIotRuleEngineRuleNodeV addNewProductRelation(MIotRuleEngineRuleRelationNewF ruleRelationAddF);
    
    void deleteRuleRelationByRuleNodeList(List<IotRuleNode> deleteRuleNodeList, List<IotRuleChain> ruleChainList);
    
    void updateRuleRelationByPrimaryKeySelective(IotRuleRelation ruleRelation);
    
    void insertRuleRelationSelective(IotRuleRelation ruleRelation);
    
    List<String> deleteDataCheck(MIotRuleEngineProductWarningConfigDeleteF mIotRuleEngineF);
}
