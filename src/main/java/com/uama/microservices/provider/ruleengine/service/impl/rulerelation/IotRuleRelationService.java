package com.uama.microservices.provider.ruleengine.service.impl.rulerelation;

import com.google.common.collect.Lists;
import com.uama.framework.common.exception.BizException;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.microservices.api.ruleengine.enums.IotRuleRelationFromTypeEnum;
import com.uama.microservices.api.ruleengine.enums.IotRuleRelationToTypeEnum;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductWarningConfigDeleteF;
import com.uama.microservices.api.ruleengine.model.form.rulerelation.MIotRuleEngineRuleRelationAddF;
import com.uama.microservices.api.ruleengine.model.form.rulerelation.MIotRuleEngineRuleRelationNewF;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.dao.iot.IotRuleRelationMapper;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleChain;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleNode;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleRelation;
import com.uama.microservices.provider.ruleengine.service.rulechain.IIotRuleChainService;
import com.uama.microservices.provider.ruleengine.service.rulenode.IIotRuleNodeService;
import com.uama.microservices.provider.ruleengine.service.rulerelation.IIotRuleRelationService;
import com.uama.microservices.provider.ruleengine.web.error.IotRuleRelationServiceErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-10 17:37
 **/
@Service
public class IotRuleRelationService implements IIotRuleRelationService {
    @Autowired
    private IotRuleRelationMapper iotRuleRelationMapper;
    
    @Autowired
    private IIotRuleChainService iotRuleChainService;
    
    @Autowired
    private IIotRuleNodeService iotRuleNodeService;
    
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteRuleRelationByRuleNodeList(List<IotRuleNode> deleteRuleNodeList, List<IotRuleChain> deleteRuleChainList) {
        List<String> ruleNodeIdList = deleteRuleNodeList.parallelStream().filter(Objects::nonNull).map(IotRuleNode::getId).collect(Collectors.toList());
        List<String> ruleChainIdList = deleteRuleChainList.parallelStream().filter(Objects::nonNull).map(IotRuleChain::getId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(ruleNodeIdList)) {
            Example example = new Example(IotRuleRelation.class);
            example.createCriteria().orIn("fromId", ruleNodeIdList).orIn("toId", ruleNodeIdList).orIn("toId", ruleChainIdList);
            iotRuleRelationMapper.deleteByExample(example);
        }
    }
    
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateRuleRelationByPrimaryKeySelective(IotRuleRelation ruleRelation) {
        iotRuleRelationMapper.updateByPrimaryKeySelective(ruleRelation);
    }
    
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void insertRuleRelationSelective(IotRuleRelation ruleRelation) {
        iotRuleRelationMapper.insertSelective(ruleRelation);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> deleteDataCheck(MIotRuleEngineProductWarningConfigDeleteF mIotRuleEngineF) {
        List<String> ruleChainIdList = Lists.newArrayList();
        if (null != mIotRuleEngineF) {
            String dataCheckId = mIotRuleEngineF.getId();
            IotRuleNode ruleNode = iotRuleNodeService.selectRuleNodeByPrimaryKey(dataCheckId);
            if (null == ruleNode || !ActorTypeMapperEnum.DATA_CHECK.name().equals(ruleNode.getNodeType())) {
                throw new BizException(IotRuleRelationServiceErrorMessage.DATA_CHECK_NODE_NOT_EXISTS);
            }
            
            IotRuleRelation ruleRelation = new IotRuleRelation();
            ruleRelation.setFromId(dataCheckId);
            List<IotRuleRelation> toRelationList = iotRuleRelationMapper.select(ruleRelation);
            ruleRelation = new IotRuleRelation();
            ruleRelation.setToId(dataCheckId);
            List<IotRuleRelation> fromRelationList = iotRuleRelationMapper.select(ruleRelation);
            if (fromRelationList.size() != 1 || toRelationList.size() != 2) {
                throw new BizException(IotRuleRelationServiceErrorMessage.DATA_CHECK_NODE_RELATION_DATA_ERROR);
            }
            
            toRelationList.forEach(relation -> iotRuleRelationMapper.deleteByPrimaryKey(relation));
            fromRelationList.forEach(relation -> iotRuleRelationMapper.deleteByPrimaryKey(relation));
            
            iotRuleNodeService.deleteRuleNodeByPrimaryKey(ruleNode.getId());
            
            ruleChainIdList.add(ruleNode.getRuleChainId());
        }
        return ruleChainIdList;
    }
    
    @Override
    public List<MIotRuleEngineRuleRelationInitV> getRuleRelationsForActorSystem(List<String> ruleChainIdList) {
        return iotRuleRelationMapper.getRuleRelationsForActorSystem(ruleChainIdList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MIotRuleEngineRuleNodeV addNewProductRelation(MIotRuleEngineRuleRelationNewF ruleRelationNewF) {
        MIotRuleEngineRuleNodeV firstNodeV = null;
        List<MIotRuleEngineRuleRelationAddF> ruleRelationList = ruleRelationNewF.getRuleRelationFList();
        List<MIotRuleEngineRuleNodeV> ruleNodeVList = ruleRelationNewF.getRuleNodeVList();
        if (CollectionUtil.isNotEmpty(ruleRelationList)) {
            ruleRelationList.stream().filter(Objects::nonNull).forEach(ruleRelation -> {
                String fromId;
                String toId;
                String fromType;
                String toType;
                if (-1 == ruleRelation.getFromIndex()) {
                    fromId = ruleRelation.getFromId();
                    fromType = IotRuleRelationFromTypeEnum.RULE_CHAIN.name();
                } else {
                    fromId = ruleNodeVList.get(ruleRelation.getFromIndex()).getId();
                    fromType = IotRuleRelationFromTypeEnum.RULE_NODE.name();
                }
                if (-1 == ruleRelation.getToIndex()) {
                    toId = ruleRelation.getToId();
                    toType = IotRuleRelationToTypeEnum.RULE_CHAIN.name();
                } else {
                    toId = ruleNodeVList.get(ruleRelation.getToIndex()).getId();
                    toType = IotRuleRelationToTypeEnum.RULE_NODE.name();
                }
                IotRuleRelation ruleRelationData = new IotRuleRelation();
                ruleRelationData.setFromId(fromId);
                ruleRelationData.setToId(toId);
                ruleRelationData.setFromType(fromType);
                ruleRelationData.setToType(toType);
                ruleRelationData.setRelationType(ruleRelation.getRelationType());
                iotRuleRelationMapper.insertSelective(ruleRelationData);
            });
            firstNodeV = ruleNodeVList.get(0);
        }
        return firstNodeV;
    }
}
