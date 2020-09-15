package com.uama.microservices.provider.ruleengine.dao.iot;

import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleNode;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface IotRuleNodeMapper extends Mapper<IotRuleNode> {
    
    List<MIotRuleEngineRuleNodeInitV> getRuleNodesForActorSystem(@Param("ruleChainIdList")List<String> ruleChainIdList);
}