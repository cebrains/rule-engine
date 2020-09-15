package com.uama.microservices.provider.ruleengine.service.rulechain;

import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductAddF;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductEndPointUpdateF;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductUpdateF;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductWarningConfigAddF;
import com.uama.microservices.api.ruleengine.model.vo.product.MIotRuleEngineProductWarningConfigAddV;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;

import java.util.List;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-10 17:33
 **/
public interface IIotRuleChainService {
    
    List<MIotRuleEngineRuleChainInitV> getRuleChainsForActorSystem(List<String> ruleChainIdList, String belongType);
    
    String addNewProductChain(MIotRuleEngineProductAddF mIotRuleEngineF);
    
    List<MIotRuleEngineRuleChainInitV> deleteProduct(List<String> productId);
    
    List<MIotRuleEngineRuleChainInitV> updateEndPointFilter(MIotRuleEngineProductEndPointUpdateF mIotRuleEngineF);
    
    MIotRuleEngineProductWarningConfigAddV addDataCheck(MIotRuleEngineProductWarningConfigAddF mIotRuleEngineF);
    
    Boolean updateProductChain(MIotRuleEngineProductUpdateF mIotRuleEngineF);
}
