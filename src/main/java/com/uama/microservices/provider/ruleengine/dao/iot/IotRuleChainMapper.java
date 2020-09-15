package com.uama.microservices.provider.ruleengine.dao.iot;

import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleChain;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface IotRuleChainMapper extends Mapper<IotRuleChain> {
    
    List<MIotRuleEngineRuleChainInitV> getRuleChainsForActorSystem(@Param("ruleChainIdList")List<String> ruleChainIdList, @Param("belongType")String belongType);
}