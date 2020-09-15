import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.uama.microservices.api.ruleengine.enums.IotRuleChainRootEnum;
import com.uama.microservices.api.ruleengine.enums.IotRuleRelationFromTypeEnum;
import com.uama.microservices.api.ruleengine.enums.IotRuleRelationRelationTypeEnum;
import com.uama.microservices.api.ruleengine.enums.IotRuleRelationToTypeEnum;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductAddF;
import com.uama.microservices.iot.stream.gateway.IotUlinkRuleEngineGateway;
import com.uama.microservices.iot.stream.model.device.MIotRuleEngineDeviceDataStreamF;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.dao.iot.IotRuleChainMapper;
import com.uama.microservices.provider.ruleengine.dao.iot.IotRuleNodeMapper;
import com.uama.microservices.provider.ruleengine.dao.iot.IotRuleRelationMapper;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleChain;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleNode;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleRelation;
import com.uama.microservices.provider.ruleengine.service.rulechain.IIotRuleChainService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-13 10:20
 **/
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ComponentScanClass.class})
public class RuleEngineTest {
    // PARAMS START testShouldAddDataViaMappers
    private static final boolean testShouldAddDataViaMappersLock = true;
    private static final String SOME_PRODUCT_ID = "18fe3759-6b4a-4072-a3f3-f5b7ed5ca4b8";
    private static final String SOME_PRODUCT_NAME = "用传-烟感火灾探测器";
    private static final String INUSER = "ad";
    private static final String ROOT_RULE_CHAIN_NAME = "Root Chain";
    // PARAMS END
    
    // PARAMS START testShouldAddDataToMongo
    private static final boolean testShouldAddDataToMongoLock = true;
    private static final String productId = "18fe3759-6b4a-4072-a3f3-f5b7ed5ca4b8";
    private static final String deviceId = "abc";
    private static final JSONObject jsonObj = JSONObject.parseObject("{fire: false, temperature: 36}");
    private static final String dataType = "DEVICE_DATA_END_POINT";
    // PARAMS END
    
    // PARAMS START testShouldInitialChainWhenAddNewProduct
    private static final boolean testShouldInitialChainWhenAddNewProductLock = true;
    private static final String productId_I = "7ef8c410-2c51-4d85-8df6-bb158fbe99fe";
    private static final String productName_I = "华立单相智能电表";
    // PARAMS END
    
    // PARAMS START testShouldAddDataToMongoAndTransportDataViaKafka
    private static final boolean testShouldAddDataToMongoAndTransportDataViaKafkaLock = true;
    private static final String productId_III = "18fe3759-6b4a-4072-a3f3-f5b7ed5ca4b8";
    private static final String deviceId_III = "dc7d0fb8-bd5e-433a-b9e7-aec790a830a4";
    private static final JSONObject jsonObj_III = JSONObject.parseObject("{fire: false, temperature: 36}");
    private static final String dataType_III = "DEVICE_DATA_END_POINT";
    // PARAMS END
    
    @Autowired
    private IotRuleChainMapper iotRuleChainMapper;
    @Autowired
    private IotRuleNodeMapper iotRuleNodeMapper;
    @Autowired
    private IotRuleRelationMapper iotRuleRelationMapper;
    @Autowired
    private IotUlinkRuleEngineGateway iotUlinkRuleEngineGateway;
    @Autowired
    private IIotRuleChainService iotRuleChainService;
    
    // simulate a device endpoint data to save in Mongo and send to kafka via a complicated rule chain
    @Test
    public void testShouldAddDataToMongoAndTransportDataViaKafka() throws Exception {
        if (testShouldAddDataToMongoAndTransportDataViaKafkaLock) {
            return;
        }
        MIotRuleEngineDeviceDataStreamF mStreamF = new MIotRuleEngineDeviceDataStreamF();
        mStreamF.setProductId(productId_III);
        mStreamF.setDeviceId(deviceId_III);
        mStreamF.setEndPointJStr(jsonObj_III.toJSONString());
        mStreamF.setDataType(dataType_III);
        iotUlinkRuleEngineGateway.receiveDataForRuleEngine(mStreamF);
        // wait data transported via kafka
        synchronized (this) {
            try {
                this.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Assert.assertTrue(true);
    }
    
    @Test
    public void testShouldInitialChainWhenAddNewProduct() throws Exception {
        if (testShouldInitialChainWhenAddNewProductLock) {
            return;
        }
        MIotRuleEngineProductAddF addF = new MIotRuleEngineProductAddF();
        addF.setProductId(productId_I);
        addF.setProductName(productName_I);
        iotRuleChainService.addNewProductChain(addF);
        Assert.assertTrue(true);
    }
    
    // simulate a device endpoint data to save in Mongo via a simple rule chain
    @Test
    public void testShouldAddDataToMongo() throws Exception {
        if (testShouldAddDataToMongoLock) {
            return;
        }
        MIotRuleEngineDeviceDataStreamF mStreamF = new MIotRuleEngineDeviceDataStreamF();
        mStreamF.setProductId(productId);
        mStreamF.setDeviceId(deviceId);
        mStreamF.setEndPointJStr(jsonObj.toJSONString());
        mStreamF.setDataType(dataType);
        iotUlinkRuleEngineGateway.receiveDataForRuleEngine(mStreamF);
        // wait Mongo insert opr done
        await().atMost(3, TimeUnit.SECONDS).until(() -> System.out.println("wait done"));
        Assert.assertTrue(true);
    }
    
    // initial test data with one root chain and one product chain which adds data to database directly
    @Test
    public void testShouldAddDataViaMappers() {
        // safe lock in case of running this test accidently
        if (testShouldAddDataViaMappersLock) {
            return;
        }
        BiMap<String, String> nodeIdBiMap = HashBiMap.create();
        IotRuleChain chainData = new IotRuleChain();
        chainData.setChainName(ROOT_RULE_CHAIN_NAME);
        chainData.setRoot(IotRuleChainRootEnum.ROOT.getCode());
        chainData.setTenantId(INUSER);
        chainData.setInuser(INUSER);
        iotRuleChainMapper.insertSelective(chainData);

        IotRuleNode nodeData = new IotRuleNode();
        nodeData.setRuleChainId(chainData.getId());
        nodeData.setNodeType(ActorTypeMapperEnum.INPUT.name());
        nodeData.setNodeName(ActorTypeMapperEnum.INPUT.name());
        nodeData.setInuser(INUSER);
        iotRuleNodeMapper.insertSelective(nodeData);
        nodeIdBiMap.put(ActorTypeMapperEnum.INPUT.name(), nodeData.getId());
        nodeData.setId(null);

        nodeData.setRuleChainId(chainData.getId());
        nodeData.setNodeType(ActorTypeMapperEnum.MESSAGE_TYPE_SWITCH.name());
        nodeData.setNodeName(ActorTypeMapperEnum.MESSAGE_TYPE_SWITCH.name());
        nodeData.setInuser(INUSER);
        iotRuleNodeMapper.insertSelective(nodeData);
        nodeIdBiMap.put(ActorTypeMapperEnum.MESSAGE_TYPE_SWITCH.name(), nodeData.getId());
        nodeData.setId(null);

        nodeData.setRuleChainId(chainData.getId());
        nodeData.setNodeType(ActorTypeMapperEnum.ROUTER.name());
        nodeData.setNodeName(ActorTypeMapperEnum.ROUTER.name());
        nodeData.setInuser(INUSER);
        iotRuleNodeMapper.insertSelective(nodeData);
        nodeIdBiMap.put(ActorTypeMapperEnum.ROUTER.name(), nodeData.getId());
        nodeData.setId(null);

        IotRuleRelation relationData = new IotRuleRelation();
        relationData.setFromId(chainData.getId());
        relationData.setFromType(IotRuleRelationFromTypeEnum.RULE_CHAIN.name());
        relationData.setToId(nodeIdBiMap.get(ActorTypeMapperEnum.INPUT.name()));
        relationData.setToType(IotRuleRelationToTypeEnum.RULE_NODE.name());
        relationData.setInuser(INUSER);
        relationData.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        iotRuleRelationMapper.insertSelective(relationData);
        relationData.setId(null);

        relationData.setFromId(nodeIdBiMap.get(ActorTypeMapperEnum.INPUT.name()));
        relationData.setFromType(IotRuleRelationFromTypeEnum.RULE_NODE.name());
        relationData.setToId(nodeIdBiMap.get(ActorTypeMapperEnum.MESSAGE_TYPE_SWITCH.name()));
        relationData.setToType(IotRuleRelationToTypeEnum.RULE_NODE.name());
        relationData.setInuser(INUSER);
        relationData.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        iotRuleRelationMapper.insertSelective(relationData);
        relationData.setId(null);

        relationData.setFromId(nodeIdBiMap.get(ActorTypeMapperEnum.MESSAGE_TYPE_SWITCH.name()));
        relationData.setFromType(IotRuleRelationFromTypeEnum.RULE_NODE.name());
        relationData.setToId(nodeIdBiMap.get(ActorTypeMapperEnum.ROUTER.name()));
        relationData.setToType(IotRuleRelationToTypeEnum.RULE_NODE.name());
        relationData.setInuser(INUSER);
        relationData.setRelationType(IotRuleRelationRelationTypeEnum.DEVICE_DATA.name());
        iotRuleRelationMapper.insertSelective(relationData);
        relationData.setId(null);
        
        String chainId = chainData.getId();
        chainData = new IotRuleChain();
        chainData.setId(chainId);
        chainData.setFirstNodeId(nodeIdBiMap.get(ActorTypeMapperEnum.INPUT.name()));
        iotRuleChainMapper.updateByPrimaryKeySelective(chainData);
        chainData.setFirstNodeId(null);
        
        chainData.setId(null);
        chainData.setChainName(SOME_PRODUCT_NAME);
        chainData.setRoot(IotRuleChainRootEnum.NOT_ROOT.getCode());
        chainData.setBelongToId(SOME_PRODUCT_ID);
        iotRuleChainMapper.insertSelective(chainData);
    
        nodeData = new IotRuleNode();
        nodeData.setRuleChainId(chainData.getId());
        nodeData.setNodeType(ActorTypeMapperEnum.INPUT.name());
        nodeData.setNodeName(ActorTypeMapperEnum.INPUT.name());
        iotRuleNodeMapper.insertSelective(nodeData);
        nodeIdBiMap.put(ActorTypeMapperEnum.INPUT.name(), nodeData.getId());
        nodeData.setId(null);
    
        nodeData.setRuleChainId(chainData.getId());
        nodeData.setNodeType(ActorTypeMapperEnum.SAVE_DB.name());
        nodeData.setNodeName(ActorTypeMapperEnum.SAVE_DB.name());
        iotRuleNodeMapper.insertSelective(nodeData);
        nodeIdBiMap.put(ActorTypeMapperEnum.SAVE_DB.name(), nodeData.getId());
        nodeData.setId(null);
    
        relationData = new IotRuleRelation();
        relationData.setFromId(chainData.getId());
        relationData.setFromType(IotRuleRelationFromTypeEnum.RULE_CHAIN.name());
        relationData.setToId(nodeIdBiMap.get(ActorTypeMapperEnum.INPUT.name()));
        relationData.setToType(IotRuleRelationToTypeEnum.RULE_NODE.name());
        relationData.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        iotRuleRelationMapper.insertSelective(relationData);
        relationData.setId(null);
    
        relationData.setFromId(nodeIdBiMap.get(ActorTypeMapperEnum.INPUT.name()));
        relationData.setFromType(IotRuleRelationFromTypeEnum.RULE_NODE.name());
        relationData.setToId(nodeIdBiMap.get(ActorTypeMapperEnum.SAVE_DB.name()));
        relationData.setToType(IotRuleRelationToTypeEnum.RULE_NODE.name());
        relationData.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        iotRuleRelationMapper.insertSelective(relationData);
        relationData.setId(null);
    
        chainId = chainData.getId();
        chainData = new IotRuleChain();
        chainData.setId(chainId);
        chainData.setFirstNodeId(nodeIdBiMap.get(ActorTypeMapperEnum.INPUT.name()));
        iotRuleChainMapper.updateByPrimaryKeySelective(chainData);
        chainData.setFirstNodeId(null);
        Assert.assertTrue(true);
    }
}
