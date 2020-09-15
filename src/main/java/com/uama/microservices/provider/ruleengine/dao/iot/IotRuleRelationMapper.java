package com.uama.microservices.provider.ruleengine.dao.iot;

import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleRelation;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface IotRuleRelationMapper extends Mapper<IotRuleRelation> {
    
    List<MIotRuleEngineRuleRelationInitV> getRuleRelationsForActorSystem(@Param("ruleChainIdList")List<String> ruleChainIdList);
}