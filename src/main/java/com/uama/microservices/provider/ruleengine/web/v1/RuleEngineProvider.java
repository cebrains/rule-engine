package com.uama.microservices.provider.ruleengine.web.v1;

import com.google.common.collect.Lists;
import com.uama.framework.core.MicroApiVersion;
import com.uama.framework.core.MicroApiVersionEnum;
import com.uama.microservices.api.ruleengine.model.form.product.*;
import com.uama.microservices.api.ruleengine.model.vo.product.MIotRuleEngineProductWarningConfigAddV;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.ActorClusterService;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice.ActorService;
import com.uama.microservices.provider.ruleengine.actorcluster.message.ActorClusterMessageType;
import com.uama.microservices.provider.ruleengine.actorcluster.message.BroadCastMessage;
import com.uama.microservices.provider.ruleengine.service.rulechain.IIotRuleChainService;
import com.uama.microservices.provider.ruleengine.service.rulenode.IIotRuleNodeService;
import com.uama.microservices.provider.ruleengine.service.rulerelation.IIotRuleRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Api(tags = {"规则引擎相关接口"})
@RestController("RuleEngineProviderV1")
@MicroApiVersion(MicroApiVersionEnum.VERSION_1)
public class RuleEngineProvider {
    @Autowired
    private IIotRuleRelationService iotRuleRelationService;
    
    @Autowired
    private IIotRuleNodeService iotRuleNodeService;
    
    @Autowired
    private IIotRuleChainService iotRuleChainService;
    
    @Autowired(required = false)
    private ActorClusterService actorClusterService;
    
    @Autowired(required = false)
    private ActorService actorService;
    
    @ApiOperation("服务一体化更新产品后 更新产品链信息")
    @PostMapping("/ruleEngine/updateProductChain")
    public ResponseEntity<Boolean> updateProductChain(@RequestBody MIotRuleEngineProductUpdateF mIotRuleEngineF) {
        if (StringUtils.isBlank(mIotRuleEngineF.getProductId())) {
            return ResponseEntity.ok(false);
        }
        Boolean updateSuccess = iotRuleChainService.updateProductChain(mIotRuleEngineF);
        if (Boolean.FALSE.equals(updateSuccess) && null != mIotRuleEngineF.getAddNew() && Boolean.TRUE.equals(mIotRuleEngineF.getAddNew())) {
            MIotRuleEngineProductAddF addF = new MIotRuleEngineProductAddF();
            addF.setProductName(mIotRuleEngineF.getProductName());
            addF.setProductId(mIotRuleEngineF.getProductId());
            addNewProductChain(addF);
        }
        return ResponseEntity.ok(Objects.requireNonNull(updateSuccess));
    }
    
    @ApiOperation("服务一体化添加产品后 初始化产品链(后期预警规则改为视图拖拽后可删除)")
    @PostMapping("/ruleEngine/addNewProductChain")
    public void addNewProductChain(@RequestBody MIotRuleEngineProductAddF mIotRuleEngineF) {
        // init default product rule chain in db
        String ruleChainId = iotRuleChainService.addNewProductChain(mIotRuleEngineF);
        // add rule chain to actor system
        if (null != actorClusterService) {
            actorClusterService.broadCast(new BroadCastMessage(ActorClusterMessageType.RULE_CHAIN_INSERT, Lists.newArrayList(ruleChainId)));
        } else {
            actorService.addRuleChainsToActorSystem(Lists.newArrayList(ruleChainId));
        }
    }
    
    @ApiOperation("服务一体化删除产品后 删除产品链(后期预警规则改为视图拖拽后可删除)")
    @PostMapping("/ruleEngine/deleteProductChain")
    public void deleteProduct(@RequestBody MIotRuleEngineProductDeleteF mIotRuleEngineF) {
        List<String> productIdList = mIotRuleEngineF.getProductIdList();
        // delete product rule chains in db
        List<MIotRuleEngineRuleChainInitV> ruleChainVList = iotRuleChainService.deleteProduct(productIdList);
        // delete rule chains from actor system
        if (null != actorClusterService) {
            actorClusterService.broadCast(new BroadCastMessage(ActorClusterMessageType.RULE_CHAIN_DELETE, (ArrayList<MIotRuleEngineRuleChainInitV>)ruleChainVList));
        } else {
            actorService.deleteRuleChainsFromActorSystem(ruleChainVList);
        }
    }
    
    @ApiOperation("服务一体化变更数据端点 相当于修改DataFilterNode的configuration(后期预警规则改为视图拖拽后可删除)")
    @PostMapping("/ruleEngine/updateEndPointFilter")
    public void updateEndPointFilter(@RequestBody MIotRuleEngineProductEndPointUpdateF mIotRuleEngineF) {
        // update product rule chain's datafilter node configuration
        List<MIotRuleEngineRuleChainInitV> ruleChainVList = iotRuleChainService.updateEndPointFilter(mIotRuleEngineF);
        // update rule chain in actor system
        if (null != actorClusterService) {
            actorClusterService.broadCast(new BroadCastMessage(ActorClusterMessageType.RULE_CHAIN_UPDATE, ruleChainVList.parallelStream().filter(Objects::nonNull).map(MIotRuleEngineRuleChainInitV::getId).collect(Collectors.toCollection(ArrayList::new))));
        } else {
            actorService.updateRuleChainsInActorSystem(ruleChainVList.parallelStream().filter(Objects::nonNull).map(MIotRuleEngineRuleChainInitV::getId).collect(Collectors.toList()));
        }
    }
    
    @ApiOperation("服务一体化新增预警配置 相当于新增DataCheckNode在savedb与datacheck之前(后期预警规则改为视图拖拽后可删除)")
    @PostMapping("/ruleEngine/addWarningConfig")
    public ResponseEntity addWarningConfig(@RequestBody MIotRuleEngineProductWarningConfigAddF mIotRuleEngineF) {
        // add product rule chain's data check node
        MIotRuleEngineProductWarningConfigAddV mIotRuleEngineV = iotRuleChainService.addDataCheck(mIotRuleEngineF);
        // update rule chain in actor system
        if (null != actorClusterService) {
            actorClusterService.broadCast(new BroadCastMessage(ActorClusterMessageType.RULE_CHAIN_UPDATE, (ArrayList<String>)mIotRuleEngineV.getRuleChainIdList()));
        } else {
            actorService.updateRuleChainsInActorSystem(mIotRuleEngineV.getRuleChainIdList());
        }
        return ResponseEntity.ok(mIotRuleEngineV.getRuleNodeIdList());
    }
    
    @ApiOperation("服务一体化更新预警配置 相当于更新DataCheckNode configuration(后期预警规则改为视图拖拽后可删除)")
    @PostMapping("/ruleEngine/updateWarningConfig")
    public void updateWarningConfig(@RequestBody MIotRuleEngineProductWarningConfigUpdateF mIotRuleEngineF) {
        // update product rule chain's data check node configuration
        List<String> ruleChainIdList = iotRuleNodeService.updateDataCheck(mIotRuleEngineF);
        // update rule chain in actor system
        if (null != actorClusterService) {
            actorClusterService.broadCast(new BroadCastMessage(ActorClusterMessageType.RULE_CHAIN_UPDATE, (ArrayList<String>)ruleChainIdList));
        } else {
            actorService.updateRuleChainsInActorSystem(ruleChainIdList);
        }
    }
    
    @ApiOperation("服务一体化删除预警配置 相当于删除DataCheckNode 并修改关系(后期预警规则改为视图拖拽后可删除)")
    @PostMapping("/ruleEngine/deleteWarningConfig")
    public void deleteWarningConfig(@RequestBody MIotRuleEngineProductWarningConfigDeleteF mIotRuleEngineF) {
        // delete product rule chain's data check node
        List<String> ruleChainIdList = iotRuleRelationService.deleteDataCheck(mIotRuleEngineF);
        // update rule chain in actor system
        if (null != actorClusterService) {
            actorClusterService.broadCast(new BroadCastMessage(ActorClusterMessageType.RULE_CHAIN_UPDATE, (ArrayList<String>)ruleChainIdList));
        } else {
            actorService.updateRuleChainsInActorSystem(ruleChainIdList);
        }
    }
}