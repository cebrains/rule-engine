package com.uama.microservices.provider.ruleengine.service.impl.rulenode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.uama.framework.common.exception.BizException;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.framework.common.util.bean.BeanCastUtil;
import com.uama.microservices.api.ruleengine.enums.IotRuleRelationFromTypeEnum;
import com.uama.microservices.api.ruleengine.enums.IotRuleRelationRelationTypeEnum;
import com.uama.microservices.api.ruleengine.enums.IotRuleRelationToTypeEnum;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductWarningConfigAddF;
import com.uama.microservices.api.ruleengine.model.form.product.MIotRuleEngineProductWarningConfigUpdateF;
import com.uama.microservices.api.ruleengine.model.form.rulerelation.MIotRuleEngineRuleRelationAddF;
import com.uama.microservices.api.ruleengine.model.form.rulerelation.MIotRuleEngineRuleRelationNewF;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ConstantFields;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.dao.iot.IotRuleNodeMapper;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleChain;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleNode;
import com.uama.microservices.provider.ruleengine.model.iot.IotRuleRelation;
import com.uama.microservices.provider.ruleengine.service.rulenode.IIotRuleNodeService;
import com.uama.microservices.provider.ruleengine.service.rulerelation.IIotRuleRelationService;
import com.uama.microservices.provider.ruleengine.web.error.IotRuleNodeServiceErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-10 17:36
 **/
@Service
public class IotRuleNodeService implements IIotRuleNodeService {
    @Autowired
    private IotRuleNodeMapper iotRuleNodeMapper;
    
    @Autowired
    private IIotRuleRelationService iotRuleRelationService;
    
    @Value("${spring.kafka.ulink.bootstrap-server}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.device.alert.topic}")
    private String alertTopic;
    @Value("${spring.kafka.consumer.device.datapoint.topic}")
    private String endPointTopic;
    @Value("${spring.kafka.consumer.device.state.topic}")
    private String deviceStatusTopic;
    @Value("${spring.kafka.ulink.username}")
    private String kafkaUsername;
    @Value("${spring.kafka.ulink.password}")
    private String kafkaPassword;
    
    @Override
    public List<MIotRuleEngineRuleNodeInitV> getRuleNodesForActorSystem(List<String> ruleChainIdList) {
        return iotRuleNodeMapper.getRuleNodesForActorSystem(ruleChainIdList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MIotRuleEngineRuleRelationNewF addNewProductNode(IotRuleChain ruleChain) {
        // add new nodes
        List<MIotRuleEngineRuleNodeV> ruleNodeList = addNewNodes(ruleChain);
        // construct relations
        List<MIotRuleEngineRuleRelationAddF> ruleRelationList = constructRelations(ruleChain);
        return MIotRuleEngineRuleRelationNewF.builder()
                .ruleNodeVList(ruleNodeList)
                .ruleRelationFList(ruleRelationList)
                .build();
    }
    
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public List<IotRuleNode> deleteNodesByRuleChainIdList(List<String> ruleChainIdList) {
        Example example = new Example(IotRuleNode.class);
        example.createCriteria().andIn("ruleChainId", ruleChainIdList);
        List<IotRuleNode> deleteRuleNodeList = iotRuleNodeMapper.selectByExample(example);
        iotRuleNodeMapper.deleteByExample(example);
        return deleteRuleNodeList;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEndPointFitler(String ruleChainId, Set<String> endPointList) {
        IotRuleNode ruleNodeTempate = new IotRuleNode();
        ruleNodeTempate.setRuleChainId(ruleChainId);
        ruleNodeTempate.setNodeType(ActorTypeMapperEnum.DATA_FILTER.name());
        List<IotRuleNode> ruleNodeList = iotRuleNodeMapper.select(ruleNodeTempate);
        if (CollectionUtil.isNotEmpty(ruleNodeList)) {
            ruleNodeList.parallelStream().filter(Objects::nonNull).forEach(ruleNode -> {
                JSONObject nodeConfiguration = JSON.parseObject(ruleNode.getNodeConfiguration());
                JSONArray jArray = new JSONArray();
                jArray.addAll(endPointList);
                nodeConfiguration.put(ConstantFields.FILTER_FIELD_LIST, jArray);
                ruleNode.setNodeConfiguration(nodeConfiguration.toJSONString());
                iotRuleNodeMapper.updateByPrimaryKeySelective(ruleNode);
            });
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotRuleNode addDataCheckNodeAndRelations(String ruleChainId, MIotRuleEngineProductWarningConfigAddF mIotRuleEngineF) {
        IotRuleNode ruleNode = new IotRuleNode();
        ruleNode.setRuleChainId(ruleChainId);
        ruleNode.setNodeType(ActorTypeMapperEnum.DATA_CHECK.name());
        ruleNode.setNodeName(ActorTypeMapperEnum.DATA_CHECK.name());
        
        Byte triggerType = mIotRuleEngineF.getTriggerType();
        String jsScript = constructJsScript(triggerType, mIotRuleEngineF.getDeviceStatus(), mIotRuleEngineF.getJudgingCondition(), mIotRuleEngineF.getEndPointName(), mIotRuleEngineF.getThresholdValue(), mIotRuleEngineF.getDataType());
        JSONObject configurationJ = new JSONObject();
        configurationJ.put(ConstantFields.JS_SCRIPT, jsScript);
        configurationJ.put(ConstantFields.TRIGGER_DEVICE_IDS, mIotRuleEngineF.getTriggerDeviceIdSet());
        ruleNode.setNodeConfiguration(configurationJ.toJSONString());
        iotRuleNodeMapper.insertSelective(ruleNode);
        
        String newDataCheckId = ruleNode.getId();
    
        List<MIotRuleEngineRuleRelationInitV> ruleChainRelations = iotRuleRelationService.getRuleRelationsForActorSystem(Lists.newArrayList(ruleChainId));
        List<MIotRuleEngineRuleRelationInitV> neededRelations = Lists.newArrayList();
        AtomicReference<String> endPointSaveDBId = getEndpointSaveDBId(ruleChainRelations, neededRelations);
        if (StringUtils.isBlank(endPointSaveDBId.get()) || neededRelations.size() < 2) {
            throw new BizException(IotRuleNodeServiceErrorMessage.RELATION_DATA_ERROR);
        }
        
        boolean isEndPoint = triggerType == 1;
        Optional<MIotRuleEngineRuleRelationInitV> saveDBRelationOpt = neededRelations.stream().filter(relation -> isEndPoint == relation.getFromId().equals(endPointSaveDBId.get())).findAny();
        if (!saveDBRelationOpt.isPresent()) {
            throw new BizException(IotRuleNodeServiceErrorMessage.SAVE_DB_RELATION_DATA_NOT_EXISTS);
        }
        MIotRuleEngineRuleRelationInitV saveDBRelation = saveDBRelationOpt.get();
        String dataCheckId = saveDBRelation.getToId();
        
        AtomicReference<String> createAlarmId = new AtomicReference<>();
        AtomicReference<String> kafkaId = new AtomicReference<>();
        getCreateAlaramAndKafkaNodeId(ruleChainRelations, createAlarmId, kafkaId, dataCheckId);
        if (StringUtils.isBlank(createAlarmId.get())) {
            throw new BizException(IotRuleNodeServiceErrorMessage.CREATE_ALARM_RELATION_DATA_NOT_EXISTS);
        }
        if (StringUtils.isBlank(kafkaId.get())) {
            throw new BizException(IotRuleNodeServiceErrorMessage.KAFKA_RELATION_DATA_NOT_EXISTS);
        }
        
        IotRuleRelation saveDbRelationData = new IotRuleRelation();
        saveDbRelationData.setFromId(saveDBRelation.getFromId());
        saveDbRelationData.setToId(newDataCheckId);
        saveDbRelationData.setFromType(saveDBRelation.getFromType());
        saveDbRelationData.setToType(IotRuleRelationToTypeEnum.RULE_NODE.name());
        saveDbRelationData.setRelationType(saveDBRelation.getRelationType());
        iotRuleRelationService.insertRuleRelationSelective(saveDbRelationData);
        
        IotRuleRelation checkFalseRelation = new IotRuleRelation();
        checkFalseRelation.setFromId(newDataCheckId);
        checkFalseRelation.setFromType(IotRuleRelationFromTypeEnum.RULE_NODE.name());
        checkFalseRelation.setToId(kafkaId.get());
        checkFalseRelation.setToType(IotRuleRelationToTypeEnum.RULE_NODE.name());
        checkFalseRelation.setRelationType(IotRuleRelationRelationTypeEnum.FALSE.name());
        iotRuleRelationService.insertRuleRelationSelective(checkFalseRelation);
        
        IotRuleRelation checkTrueRelation = new IotRuleRelation();
        checkTrueRelation.setFromId(newDataCheckId);
        checkTrueRelation.setFromType(IotRuleRelationFromTypeEnum.RULE_NODE.name());
        checkTrueRelation.setToId(createAlarmId.get());
        checkTrueRelation.setToType(IotRuleRelationToTypeEnum.RULE_NODE.name());
        checkTrueRelation.setRelationType(IotRuleRelationRelationTypeEnum.TRUE.name());
        iotRuleRelationService.insertRuleRelationSelective(checkTrueRelation);
        
        return ruleNode;
    }
    
    private void getCreateAlaramAndKafkaNodeId(List<MIotRuleEngineRuleRelationInitV> ruleChainRelations, AtomicReference<String> createAlarmId, AtomicReference<String> kafkaId, String dataCheckId) {
        ruleChainRelations.stream().filter(relation -> relation.getFromId().equals(dataCheckId)).forEach(relation -> {
            if (relation.getToNodeType().equals(ActorTypeMapperEnum.CREATE_ALARM.name())) {
                if (StringUtils.isNotBlank(createAlarmId.get())) {
                    throw new BizException(IotRuleNodeServiceErrorMessage.CREATE_ALARM_RELATION_DATA_ERROR);
                } else {
                    createAlarmId.set(relation.getToId());
                }
            }
            if (relation.getToNodeType().equals(ActorTypeMapperEnum.KAFKA.name())) {
                if (StringUtils.isNotBlank(kafkaId.get())) {
                    throw new BizException(IotRuleNodeServiceErrorMessage.KAFKA_RELATION_DATA_ERROR);
                } else {
                    kafkaId.set(relation.getToId());
                }
            }
        });
    }
    
    private AtomicReference<String> getEndpointSaveDBId(List<MIotRuleEngineRuleRelationInitV> ruleChainRelations, List<MIotRuleEngineRuleRelationInitV> neededRelations) {
        AtomicReference<String> endPointSaveDBId = new AtomicReference<>();
        ruleChainRelations.forEach(relation -> {
            if (relation.getFromType().equals(IotRuleRelationFromTypeEnum.RULE_NODE.name())) {
                if (relation.getFromNodeType().equals(ActorTypeMapperEnum.DATA_FILTER.name())) {
                    endPointSaveDBId.set(relation.getToId());
                }
                if (relation.getFromNodeType().equals(ActorTypeMapperEnum.SAVE_DB.name()) && relation.getToNodeType().equals(ActorTypeMapperEnum.DATA_CHECK.name())) {
                    neededRelations.add(relation);
                }
            }
        });
        return endPointSaveDBId;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> updateDataCheck(MIotRuleEngineProductWarningConfigUpdateF mIotRuleEngineF) {
        List<String> ruleChainIdList = Lists.newArrayList();
        if (null != mIotRuleEngineF) {
            IotRuleNode ruleNode = iotRuleNodeMapper.selectByPrimaryKey(mIotRuleEngineF.getId());
            if (null == ruleNode) {
                throw new BizException(IotRuleNodeServiceErrorMessage.RULE_NODE_NOT_EXISTS);
            }
            
            JSONObject configurationJ = JSON.parseObject(ruleNode.getNodeConfiguration());
            String jsScript = constructJsScript(mIotRuleEngineF.getTriggerType(), mIotRuleEngineF.getDeviceStatus(), mIotRuleEngineF.getJudgingCondition(), mIotRuleEngineF.getEndPointName(), mIotRuleEngineF.getThresholdValue(), mIotRuleEngineF.getDataType());
            configurationJ.put(ConstantFields.JS_SCRIPT, jsScript);
            configurationJ.put(ConstantFields.TRIGGER_DEVICE_IDS, mIotRuleEngineF.getTriggerDeviceIdSet());
            ruleNode.setNodeConfiguration(configurationJ.toJSONString());
            iotRuleNodeMapper.updateByPrimaryKeySelective(ruleNode);
            
            ruleChainIdList.add(ruleNode.getRuleChainId());
        }
        return ruleChainIdList;
    }
    
    @Override
    public IotRuleNode selectRuleNodeByPrimaryKey(String id) {
        return iotRuleNodeMapper.selectByPrimaryKey(id);
    }
    
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteRuleNodeByPrimaryKey(String id) {
        iotRuleNodeMapper.deleteByPrimaryKey(id);
    }
    
    private String constructJsScript(Byte triggerType, Byte deviceStatus, String judgingCondition, String endPointName, String value, Byte dataType) {
        // endPointName is a pure number will bring a error while running js script, cause number is a constant value
        if (NumberUtils.isParsable(endPointName)) {
            endPointName = ConstantFields.PURE_NUMBER_PREFIX + endPointName;
        }
        String judgingConditionConvert = null;
        String deviceStatusConvert = "0";
        if (deviceStatus != null && 2 != deviceStatus) {
            deviceStatusConvert =  deviceStatus.toString();
        }
        if (!NumberUtils.isParsable(value)) {
            value = "\"" + value + "\"";
        }
        if (triggerType == 1) {
            switch(judgingCondition) {
                case "≠":
                    judgingConditionConvert = "!=";
                    break;
                case "=":
                    judgingConditionConvert = "==";
                    break;
                default:
                    judgingConditionConvert = judgingCondition;
                    break;
            }
        }
        String endPointNameConvert = triggerType == 1 ? endPointName : ConstantFields.STATUS;
        String valueConvert = triggerType == 1 ? value : deviceStatusConvert;
        judgingConditionConvert = triggerType == 1 ? judgingConditionConvert : "==";
        String returnStr = endPointNameConvert + judgingConditionConvert + valueConvert;
        if (null != dataType && 1 == dataType) {
            returnStr = endPointNameConvert + ".toLowerCase()" + judgingConditionConvert + valueConvert.toLowerCase();
        }
        return String.format(ConstantFields.JS_SCRIPT_II, returnStr);
    }
    
    private List<MIotRuleEngineRuleNodeV> addNewNodes(IotRuleChain ruleChain) {
        List<IotRuleNode> ruleNodeList = Lists.newArrayList();
        // input (index 0)
        IotRuleNode input = new IotRuleNode();
        input.setRuleChainId(ruleChain.getId());
        input.setNodeType(ActorTypeMapperEnum.INPUT.name());
        input.setNodeName(ActorTypeMapperEnum.INPUT.name());
        iotRuleNodeMapper.insertSelective(input);
        ruleNodeList.add(input);
        // messageTypeSwitch (index 1)
        IotRuleNode messageTypeSwitch = new IotRuleNode();
        messageTypeSwitch.setRuleChainId(ruleChain.getId());
        messageTypeSwitch.setNodeType(ActorTypeMapperEnum.MESSAGE_TYPE_SWITCH.name());
        messageTypeSwitch.setNodeName(ActorTypeMapperEnum.MESSAGE_TYPE_SWITCH.name());
        iotRuleNodeMapper.insertSelective(messageTypeSwitch);
        ruleNodeList.add(messageTypeSwitch);
        // end point data filter (index 2)
        IotRuleNode dataFilter = new IotRuleNode();
        dataFilter.setRuleChainId(ruleChain.getId());
        dataFilter.setNodeType(ActorTypeMapperEnum.DATA_FILTER.name());
        dataFilter.setNodeName(ActorTypeMapperEnum.DATA_FILTER.name());
        iotRuleNodeMapper.insertSelective(dataFilter);
        ruleNodeList.add(dataFilter);
        // end point save db (index 3)
        IotRuleNode saveDataBase = new IotRuleNode();
        saveDataBase.setRuleChainId(ruleChain.getId());
        saveDataBase.setNodeType(ActorTypeMapperEnum.SAVE_DB.name());
        saveDataBase.setNodeName(ActorTypeMapperEnum.SAVE_DB.name());
        iotRuleNodeMapper.insertSelective(saveDataBase);
        ruleNodeList.add(saveDataBase);
        // device status save db (index 4)
        IotRuleNode saveDataBase2 = new IotRuleNode();
        saveDataBase2.setRuleChainId(ruleChain.getId());
        saveDataBase2.setNodeType(ActorTypeMapperEnum.SAVE_DB.name());
        saveDataBase2.setNodeName(ActorTypeMapperEnum.SAVE_DB.name());
        iotRuleNodeMapper.insertSelective(saveDataBase2);
        ruleNodeList.add(saveDataBase2);
        // end point data check (index 5)
        IotRuleNode alarmCheck = new IotRuleNode();
        alarmCheck.setRuleChainId(ruleChain.getId());
        alarmCheck.setNodeType(ActorTypeMapperEnum.DATA_CHECK.name());
        alarmCheck.setNodeName(ActorTypeMapperEnum.DATA_CHECK.name());
        JSONObject endPointDataCheck = new JSONObject();
        endPointDataCheck.put("deadEnd", true);
        alarmCheck.setNodeConfiguration(endPointDataCheck.toJSONString());
        iotRuleNodeMapper.insertSelective(alarmCheck);
        ruleNodeList.add(alarmCheck);
        // device status data check (index 6)
        IotRuleNode alarmCheck2 = new IotRuleNode();
        alarmCheck2.setRuleChainId(ruleChain.getId());
        alarmCheck2.setNodeType(ActorTypeMapperEnum.DATA_CHECK.name());
        alarmCheck2.setNodeName(ActorTypeMapperEnum.DATA_CHECK.name());
        JSONObject deviceStatusDataCheck = new JSONObject();
        deviceStatusDataCheck.put("deadEnd", true);
        alarmCheck2.setNodeConfiguration(deviceStatusDataCheck.toJSONString());
        iotRuleNodeMapper.insertSelective(alarmCheck2);
        ruleNodeList.add(alarmCheck2);
        // end point check false kafka (index 7)
        IotRuleNode kafka = new IotRuleNode();
        kafka.setRuleChainId(ruleChain.getId());
        kafka.setNodeType(ActorTypeMapperEnum.KAFKA.name());
        kafka.setNodeName(ActorTypeMapperEnum.KAFKA.name());
        JSONObject kafkaConfigurationJ = new JSONObject();
        kafkaConfigurationJ.put(ConstantFields.TOPIC_PATTERN_II, this.alertTopic);
        kafkaConfigurationJ.put(ConstantFields.BOOTSTRAP_SERVERS, this.bootstrapServers);
        JSONObject otherProperties = new JSONObject();
        otherProperties.put(ConstantFields.SASL_JAAS_CONFIG, String.format(ConstantFields.SASL_JAAS_CONFIG_TEMPLATE, this.kafkaUsername, this.kafkaPassword));
        kafkaConfigurationJ.put(ConstantFields.OTHER_PROPERTIES, otherProperties);
        kafka.setNodeConfiguration(kafkaConfigurationJ.toJSONString());
        iotRuleNodeMapper.insertSelective(kafka);
        ruleNodeList.add(kafka);
        // end point check true create alarm (index 8)
        IotRuleNode createAlarm = new IotRuleNode();
        createAlarm.setRuleChainId(ruleChain.getId());
        createAlarm.setNodeType(ActorTypeMapperEnum.CREATE_ALARM.name());
        createAlarm.setNodeName(ActorTypeMapperEnum.CREATE_ALARM.name());
        iotRuleNodeMapper.insertSelective(createAlarm);
        ruleNodeList.add(createAlarm);
        // end point check true kafka (index 9)
        IotRuleNode kafka2 = new IotRuleNode();
        kafka2.setRuleChainId(ruleChain.getId());
        kafka2.setNodeType(ActorTypeMapperEnum.KAFKA.name());
        kafka2.setNodeName(ActorTypeMapperEnum.KAFKA.name());
        kafkaConfigurationJ = new JSONObject();
        kafkaConfigurationJ.put(ConstantFields.TOPIC_PATTERN_II, this.alertTopic);
        kafkaConfigurationJ.put(ConstantFields.BOOTSTRAP_SERVERS, this.bootstrapServers);
        otherProperties = new JSONObject();
        otherProperties.put(ConstantFields.SASL_JAAS_CONFIG, String.format(ConstantFields.SASL_JAAS_CONFIG_TEMPLATE, this.kafkaUsername, this.kafkaPassword));
        kafkaConfigurationJ.put(ConstantFields.OTHER_PROPERTIES, otherProperties);
        kafka2.setNodeConfiguration(kafkaConfigurationJ.toJSONString());
        iotRuleNodeMapper.insertSelective(kafka2);
        ruleNodeList.add(kafka2);
        // deivce status check false kafka (index 10)
        IotRuleNode kafka3 = new IotRuleNode();
        kafka3.setRuleChainId(ruleChain.getId());
        kafka3.setNodeType(ActorTypeMapperEnum.KAFKA.name());
        kafka3.setNodeName(ActorTypeMapperEnum.KAFKA.name());
        kafkaConfigurationJ = new JSONObject();
        kafkaConfigurationJ.put(ConstantFields.TOPIC_PATTERN_II, this.alertTopic);
        kafkaConfigurationJ.put(ConstantFields.BOOTSTRAP_SERVERS, this.bootstrapServers);
        otherProperties = new JSONObject();
        otherProperties.put(ConstantFields.SASL_JAAS_CONFIG, String.format(ConstantFields.SASL_JAAS_CONFIG_TEMPLATE, this.kafkaUsername, this.kafkaPassword));
        kafkaConfigurationJ.put(ConstantFields.OTHER_PROPERTIES, otherProperties);
        kafka3.setNodeConfiguration(kafkaConfigurationJ.toJSONString());
        iotRuleNodeMapper.insertSelective(kafka3);
        ruleNodeList.add(kafka3);
        // device status check true create alarm (index 11)
        IotRuleNode createAlarm2 = new IotRuleNode();
        createAlarm2.setRuleChainId(ruleChain.getId());
        createAlarm2.setNodeType(ActorTypeMapperEnum.CREATE_ALARM.name());
        createAlarm2.setNodeName(ActorTypeMapperEnum.CREATE_ALARM.name());
        iotRuleNodeMapper.insertSelective(createAlarm2);
        ruleNodeList.add(createAlarm2);
        // device status check true kafka (index 12)
        IotRuleNode kafka4 = new IotRuleNode();
        kafka4.setRuleChainId(ruleChain.getId());
        kafka4.setNodeType(ActorTypeMapperEnum.KAFKA.name());
        kafka4.setNodeName(ActorTypeMapperEnum.KAFKA.name());
        kafkaConfigurationJ = new JSONObject();
        kafkaConfigurationJ.put(ConstantFields.TOPIC_PATTERN_II, this.alertTopic);
        kafkaConfigurationJ.put(ConstantFields.BOOTSTRAP_SERVERS, this.bootstrapServers);
        otherProperties = new JSONObject();
        otherProperties.put(ConstantFields.SASL_JAAS_CONFIG, String.format(ConstantFields.SASL_JAAS_CONFIG_TEMPLATE, this.kafkaUsername, this.kafkaPassword));
        kafkaConfigurationJ.put(ConstantFields.OTHER_PROPERTIES, otherProperties);
        kafka4.setNodeConfiguration(kafkaConfigurationJ.toJSONString());
        iotRuleNodeMapper.insertSelective(kafka4);
        ruleNodeList.add(kafka4);
        // end point kafka (index 13)
        IotRuleNode kafka5 = new IotRuleNode();
        kafka5.setRuleChainId(ruleChain.getId());
        kafka5.setNodeType(ActorTypeMapperEnum.KAFKA.name());
        kafka5.setNodeName(ActorTypeMapperEnum.KAFKA.name());
        kafkaConfigurationJ = new JSONObject();
        kafkaConfigurationJ.put(ConstantFields.TOPIC_PATTERN_II, this.endPointTopic);
        kafkaConfigurationJ.put(ConstantFields.BOOTSTRAP_SERVERS, this.bootstrapServers);
        otherProperties = new JSONObject();
        otherProperties.put(ConstantFields.SASL_JAAS_CONFIG, String.format(ConstantFields.SASL_JAAS_CONFIG_TEMPLATE, this.kafkaUsername, this.kafkaPassword));
        kafkaConfigurationJ.put(ConstantFields.OTHER_PROPERTIES, otherProperties);
        kafka5.setNodeConfiguration(kafkaConfigurationJ.toJSONString());
        iotRuleNodeMapper.insertSelective(kafka5);
        ruleNodeList.add(kafka5);
        // deivce status kafka (index 14)
        IotRuleNode kafka6 = new IotRuleNode();
        kafka6.setRuleChainId(ruleChain.getId());
        kafka6.setNodeType(ActorTypeMapperEnum.KAFKA.name());
        kafka6.setNodeName(ActorTypeMapperEnum.KAFKA.name());
        kafkaConfigurationJ = new JSONObject();
        kafkaConfigurationJ.put(ConstantFields.TOPIC_PATTERN_II, this.deviceStatusTopic);
        kafkaConfigurationJ.put(ConstantFields.BOOTSTRAP_SERVERS, this.bootstrapServers);
        otherProperties = new JSONObject();
        otherProperties.put(ConstantFields.SASL_JAAS_CONFIG, String.format(ConstantFields.SASL_JAAS_CONFIG_TEMPLATE, this.kafkaUsername, this.kafkaPassword));
        kafkaConfigurationJ.put(ConstantFields.OTHER_PROPERTIES, otherProperties);
        kafka6.setNodeConfiguration(kafkaConfigurationJ.toJSONString());
        iotRuleNodeMapper.insertSelective(kafka6);
        ruleNodeList.add(kafka6);
        
        return BeanCastUtil.castList(ruleNodeList, MIotRuleEngineRuleNodeV.class);
    }
    
    private List<MIotRuleEngineRuleRelationAddF> constructRelations(IotRuleChain ruleChain) {
        List<MIotRuleEngineRuleRelationAddF> ruleRelationList = Lists.newArrayList();
        // rule chain -> input
        MIotRuleEngineRuleRelationAddF ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(-1);
        ruleRelationAddF.setFromId(ruleChain.getId());
        ruleRelationAddF.setToIndex(0);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        ruleRelationList.add(ruleRelationAddF);
        // input -> message type switch
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(0);
        ruleRelationAddF.setToIndex(1);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        ruleRelationList.add(ruleRelationAddF);
        // message type switch -> end point data filter
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(1);
        ruleRelationAddF.setToIndex(2);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.END_POINT.name());
        ruleRelationList.add(ruleRelationAddF);
        // end point data filter -> end point save db
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(2);
        ruleRelationAddF.setToIndex(3);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        ruleRelationList.add(ruleRelationAddF);
        // message type switch -> device status save db
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(1);
        ruleRelationAddF.setToIndex(4);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.DEVICE_STATUS.name());
        ruleRelationList.add(ruleRelationAddF);
        // end point save db -> end point data check
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(3);
        ruleRelationAddF.setToIndex(5);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        ruleRelationList.add(ruleRelationAddF);
        // device status save db -> device status data check
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(4);
        ruleRelationAddF.setToIndex(6);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        ruleRelationList.add(ruleRelationAddF);
        // end point data check -> end point data check false kafka
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(5);
        ruleRelationAddF.setToIndex(7);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.FALSE.name());
        ruleRelationList.add(ruleRelationAddF);
        // end point data check -> end point data check true create alarm
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(5);
        ruleRelationAddF.setToIndex(8);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.TRUE.name());
        ruleRelationList.add(ruleRelationAddF);
        // end point data check true create alarm -> end point data check true kafka
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(8);
        ruleRelationAddF.setToIndex(9);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        ruleRelationList.add(ruleRelationAddF);
        // device status check data -> device status check false kafka
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(6);
        ruleRelationAddF.setToIndex(10);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.FALSE.name());
        ruleRelationList.add(ruleRelationAddF);
        // device status check data -> device status check true create alarm
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(6);
        ruleRelationAddF.setToIndex(11);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.TRUE.name());
        ruleRelationList.add(ruleRelationAddF);
        // device status check true create alarm -> device status check true kafka
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(11);
        ruleRelationAddF.setToIndex(12);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        ruleRelationList.add(ruleRelationAddF);
        // end point save db -> end point kafka
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(3);
        ruleRelationAddF.setToIndex(13);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        ruleRelationList.add(ruleRelationAddF);
        // device status save db -> device status kafka
        ruleRelationAddF = new MIotRuleEngineRuleRelationAddF();
        ruleRelationAddF.setFromIndex(4);
        ruleRelationAddF.setToIndex(14);
        ruleRelationAddF.setRelationType(IotRuleRelationRelationTypeEnum.SUCCESS.name());
        ruleRelationList.add(ruleRelationAddF);
        return ruleRelationList;
    }
}
