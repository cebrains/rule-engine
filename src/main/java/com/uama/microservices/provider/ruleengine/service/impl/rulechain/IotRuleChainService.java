package com.uama.microservices.provider.ruleengine.service.impl.rulechain;

import com.google.common.collect.Lists;
import com.uama.framework.common.exception.BizException;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.framework.common.util.bean.BeanCastUtil;
import com.uama.microservices.api.ruleengine.enums.IotRuleChainBelongTypeEnum;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductAddF;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductEndPointUpdateF;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductUpdateF;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductWarningConfigAddF;
import com.uama.microservices.api.ruleengine.model.form.rulerelation.MIotRuleEngineRuleRelationNewF;
import com.uama.microservices.api.ruleengine.model.vo.product.MIotRuleEngineProductWarningConfigAddV;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeV;
import com.uama.microservices.provider.ruleengine.dao.iot.IotRuleChainMapper;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleChain;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleNode;
import com.uama.microservices.provider.ruleengine.service.rulechain.IIotRuleChainService;
import com.uama.microservices.provider.ruleengine.service.rulenode.IIotRuleNodeService;
import com.uama.microservices.provider.ruleengine.service.rulerelation.IIotRuleRelationService;
import com.uama.microservices.provider.ruleengine.web.error.IotRuleChainServiceErrorMessage;
import org.apache.commons.lang3.StringUtils;
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
 * @create: 2019-05-10 17:36
 **/
@Service
public class IotRuleChainService implements IIotRuleChainService {
    
    @Autowired
    private IotRuleChainMapper iotRuleChainMapper;
    @Autowired
    private IIotRuleNodeService iotRuleNodeService;
    @Autowired
    private IIotRuleRelationService iotRuleRelationService;
    
    @Override
    public Boolean updateProductChain(MIotRuleEngineProductUpdateF mIotRuleEngineF) {
        if (null == mIotRuleEngineF || StringUtils.isBlank(mIotRuleEngineF.getProductId())) {
            return false;
        }
        Example example = new Example(IotRuleChain.class);
        example.createCriteria().andEqualTo("belongToId", mIotRuleEngineF.getProductId());
        IotRuleChain updateData = new IotRuleChain();
        updateData.setChainName(mIotRuleEngineF.getProductName());
        return iotRuleChainMapper.updateByExampleSelective(updateData, example) > 0;
    }
    
    @Override
    public List<MIotRuleEngineRuleChainInitV> getRuleChainsForActorSystem(List<String> ruleChainIdList, String belongType) {
        return iotRuleChainMapper.getRuleChainsForActorSystem(ruleChainIdList, belongType);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addNewProductChain(MIotRuleEngineProductAddF mIotRuleEngineF) {
        IotRuleChain ruleChain = new IotRuleChain();
        ruleChain.setBelongType(IotRuleChainBelongTypeEnum.PRODUCT_CHAIN.name());
        ruleChain.setBelongToId(mIotRuleEngineF.getProductId());
        ruleChain.setChainName(mIotRuleEngineF.getProductName());
        iotRuleChainMapper.insertSelective(ruleChain);
        // add rule nodes and init rule relation index list
        MIotRuleEngineRuleRelationNewF ruleRelationNewF = iotRuleNodeService.addNewProductNode(ruleChain);
        // add rule relations
        MIotRuleEngineRuleNodeV firstNodeV = iotRuleRelationService.addNewProductRelation(ruleRelationNewF);
        // update rule chain first node id
        ruleChain.setFirstNodeId(firstNodeV.getId());
        iotRuleChainMapper.updateByPrimaryKeySelective(ruleChain);
        return ruleChain.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MIotRuleEngineRuleChainInitV> deleteProduct(List<String> productIdList) {
        Example example = new Example(IotRuleChain.class);
        example.createCriteria().andIn("belongToId", productIdList);
        // get pre-delete rule chain list
        List<IotRuleChain> deleteRuleChainList = iotRuleChainMapper.selectByExample(example);
        if (CollectionUtil.isNotEmpty(deleteRuleChainList)) {
            List<String> ruleChainIdList = deleteRuleChainList.stream().filter(Objects::nonNull).map(IotRuleChain::getId).collect(Collectors.toList());
            // get deleted rule node list
            List<IotRuleNode> deleteRuleNodeList = iotRuleNodeService.deleteNodesByRuleChainIdList(ruleChainIdList);
            if (CollectionUtil.isNotEmpty(deleteRuleNodeList)) {
                // delete rule relations
                iotRuleRelationService.deleteRuleRelationByRuleNodeList(deleteRuleNodeList, deleteRuleChainList);
            }
            iotRuleChainMapper.deleteByExample(example);
            return BeanCastUtil.castList(deleteRuleChainList, MIotRuleEngineRuleChainInitV.class);
        }
        return Lists.newArrayList();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MIotRuleEngineRuleChainInitV> updateEndPointFilter(MIotRuleEngineProductEndPointUpdateF mIotRuleEngineF) {
        // get rule chain id
        IotRuleChain ruleChainTemplate = new IotRuleChain();
        ruleChainTemplate.setBelongToId(mIotRuleEngineF.getProductId());
        List<IotRuleChain> ruleChainList = iotRuleChainMapper.select(ruleChainTemplate);
        if (CollectionUtil.isNotEmpty(ruleChainList)) {
            // update node configuration
            return ruleChainList.parallelStream().filter(Objects::nonNull).map(ruleChain -> {
                iotRuleNodeService.updateEndPointFitler(ruleChain.getId(), mIotRuleEngineF.getEndPointList());
                return BeanCastUtil.castBean(ruleChain, MIotRuleEngineRuleChainInitV.class);
            }).collect(Collectors.toList());
        } else {
            throw new BizException(IotRuleChainServiceErrorMessage.RULE_CHAIN_NOT_EXISTS, mIotRuleEngineF.getProductId());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MIotRuleEngineProductWarningConfigAddV addDataCheck(MIotRuleEngineProductWarningConfigAddF mIotRuleEngineF) {
        MIotRuleEngineProductWarningConfigAddV mIotRuleEngineV = null;
        if (null != mIotRuleEngineF) {
            mIotRuleEngineV = new MIotRuleEngineProductWarningConfigAddV();
            List<String> ruleChainIdList = Lists.newArrayList();
            List<String> ruleNodeIdList = Lists.newArrayList();
            
            String productId = mIotRuleEngineF.getProductId();
            if (StringUtils.isBlank(productId)) {
                throw new BizException(IotRuleChainServiceErrorMessage.PRODUCT_ID_BLANK);
            }
            
            IotRuleChain iotRuleChain = new IotRuleChain();
            iotRuleChain.setBelongToId(productId);
            List<IotRuleChain> ruleChainList = iotRuleChainMapper.select(iotRuleChain);
            if (CollectionUtil.isNotEmpty(ruleChainList)) {
                ruleChainList.forEach(ruleChain -> {
                    ruleChainIdList.add(ruleChain.getId());
                    IotRuleNode ruleNode = iotRuleNodeService.addDataCheckNodeAndRelations(ruleChain.getId(), mIotRuleEngineF);
                    ruleNodeIdList.add(ruleNode.getId());
                });
            } else {
                throw new BizException(IotRuleChainServiceErrorMessage.RULE_CHAIN_NOT_EXISTS, productId);
            }
            
            mIotRuleEngineV.setRuleChainIdList(ruleChainIdList);
            mIotRuleEngineV.setRuleNodeIdList(ruleNodeIdList);
        }
        return mIotRuleEngineV;
    }
}
