package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.nodeactor;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ConstantFields;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor.DefaultKafkaNodeProcessor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.hlog.AbstractHumanReadableLog.ERROR_STR;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-09 20:04
 **/
public class KafkaNodeActor extends NodeActor<DefaultKafkaNodeProcessor, KafkaNodeActor.KafkaNodeConfiguration> {
    
    private final KafkaNodeConfiguration configuration;
    private final Producer<Object, String> producer;
    
    protected Object[] args;
    
    public KafkaNodeActor(Map<ActorTypeMapperEnum, ActorRef> actorHolderMap,
                          Map<String, MIotRuleEngineRuleChainInitV> chainIdMap,
                          List<MIotRuleEngineRuleRelationInitV> toRelationList,
                          List<MIotRuleEngineRuleRelationInitV> fromRelationList,
                          MIotRuleEngineRuleNodeInitV nodeInitV,
                          Object... args) {
        super(actorHolderMap, chainIdMap, toRelationList, fromRelationList, nodeInitV);
        this.configuration = initNodeConfiguration(nodeInitV);
        this.producer = initKafkaProducer(this.configuration);
        this.args = args;
    }
    
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(RootRouterUpdateMessage.class, super.actorProcessor::processRootRouterUpdateMessage)
                .match(NodeDeleteMessage.class, super.actorProcessor::processNodeDeleteMessage)
                .match(NodeNotExistsMessage.class, super.actorProcessor::processNodeNotExistsMessage)
                .match(NodeAlarmMessage.class, super.actorProcessor::processNodeAlarmMessage)
                .match(NodeTriggerMessage.class, super.actorProcessor::processNodeTriggerMessage)
                .match(NodeDataMessage.class, super.actorProcessor::processNodeDataMessage)
                .match(NodeInitMessage.class, super.actorProcessor::processNodeInitMessage)
                .matchAny(super.actorProcessor::processUnknownMessage)
                .build();
    }
    
    @Override
    protected void initActorProcessor() {
        super.actorProcessor = new DefaultKafkaNodeProcessor(this);
    }
    
    private Producer<Object, String> initKafkaProducer(KafkaNodeConfiguration configuration) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, configuration.getClientId());
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.getBootstrapServers());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, configuration.getValueSerializer());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, configuration.getKeySerializer());
        properties.put(ProducerConfig.ACKS_CONFIG, configuration.getAcks());
        properties.put(ProducerConfig.RETRIES_CONFIG, configuration.getRetries());
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, configuration.getBatchSize());
        properties.put(ProducerConfig.LINGER_MS_CONFIG, configuration.getLinger());
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, configuration.getBufferMemory());
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, configuration.getSecurityProtocolConfig());
        properties.put(SaslConfigs.SASL_MECHANISM, configuration.getSaslMechanism());
        if (configuration.getOtherProperties() != null) {
            configuration.getOtherProperties().forEach(properties::put);
        }
        return new KafkaProducer<>(properties);
    }
    
    @Override
    protected KafkaNodeConfiguration initNodeConfiguration(MIotRuleEngineRuleNodeInitV nodeInitV) {
        JSONObject configJ = JSON.parseObject(nodeInitV.getNodeConfiguration());
        KafkaNodeConfiguration defaultConfiguration = new KafkaNodeConfiguration();
        JSONObject defualtJ = (JSONObject)JSON.toJSON(defaultConfiguration);
        if (null != configJ) {
            configJ.forEach(defualtJ::put);
        }
        KafkaNodeConfiguration customConfig = JSON.parseObject(defualtJ.toJSONString(), KafkaNodeConfiguration.class);
        try {
            customConfig.setClientId(InetAddress.getLocalHost().getHostName() + "/" + this.self().path() + customConfig.getClientId() + nodeInitV.getId());
        } catch (UnknownHostException e) {
            hLog.logWhoOccursWhenError(logWho, ERROR_STR, "init node configuration", e.getMessage());
        }
        return customConfig;
    }
    
    @Override
    public void postStop() throws Exception {
        if (this.producer != null) {
            try {
                this.producer.close();
            } catch (Exception e) {
                super.hLog.logWhoOccursWhenError(super.logWho, ERROR_STR, "stop kafka producer", e.getMessage());
            }
        }
        super.postStop();
    }
    
    @Override
    public ActorTypeMapperEnum getType() {
        return ActorTypeMapperEnum.KAFKA;
    }
    
    public Producer<Object, String> getProducer() {
        return this.producer;
    }
    
    public KafkaNodeConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public static class KafkaNodeConfiguration {
        private String clientId;
        private String topicPattern;
        private String bootstrapServers;
        private int retries;
        private int batchSize;
        private int linger;
        private int bufferMemory;
        private String acks;
        private String keySerializer;
        private String valueSerializer;
        private int maxBlockMS;
        private String saslMechanism;
        private String securityProtocolConfig;
        private JSONObject otherProperties;
        
        KafkaNodeConfiguration() {
            this.clientId = ConstantFields.CLIENT_ID;
            this.topicPattern = ConstantFields.TOPIC_PATTERN;
            this.bootstrapServers = ConstantFields.BOOT_STRAP_SERVERS;
            this.retries = 0;
            this.batchSize = 16384;
            this.linger = 0;
            this.bufferMemory = 33554432;
            this.acks = ConstantFields.ACKS;
            this.maxBlockMS = 5000;
            this.keySerializer = StringSerializer.class.getName();
            this.valueSerializer = StringSerializer.class.getName();
            // SASL CONFIG
            this.saslMechanism = ConstantFields.SASL_MECHANISM;
            this.securityProtocolConfig = ConstantFields.SECURITY_PROTOCOL_CONFIG;
        }
    
        public String getClientId() {
            return clientId;
        }
    
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    
        public String getTopicPattern() {
            return topicPattern;
        }
    
        public void setTopicPattern(String topicPattern) {
            this.topicPattern = topicPattern;
        }
    
        public String getBootstrapServers() {
            return bootstrapServers;
        }
    
        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }
    
        public int getRetries() {
            return retries;
        }
    
        public void setRetries(int retries) {
            this.retries = retries;
        }
    
        public int getBatchSize() {
            return batchSize;
        }
    
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    
        public int getLinger() {
            return linger;
        }
    
        public void setLinger(int linger) {
            this.linger = linger;
        }
    
        public int getBufferMemory() {
            return bufferMemory;
        }
    
        public void setBufferMemory(int bufferMemory) {
            this.bufferMemory = bufferMemory;
        }
    
        public String getAcks() {
            return acks;
        }
    
        public void setAcks(String acks) {
            this.acks = acks;
        }
    
        public String getKeySerializer() {
            return keySerializer;
        }
    
        public void setKeySerializer(String keySerializer) {
            this.keySerializer = keySerializer;
        }
    
        public String getValueSerializer() {
            return valueSerializer;
        }
    
        public void setValueSerializer(String valueSerializer) {
            this.valueSerializer = valueSerializer;
        }
    
        public int getMaxBlockMS() {
            return maxBlockMS;
        }
    
        public void setMaxBlockMS(int maxBlockMS) {
            this.maxBlockMS = maxBlockMS;
        }
    
        public String getSaslMechanism() {
            return saslMechanism;
        }
    
        public void setSaslMechanism(String saslMechanism) {
            this.saslMechanism = saslMechanism;
        }
    
        public String getSecurityProtocolConfig() {
            return securityProtocolConfig;
        }
    
        public void setSecurityProtocolConfig(String securityProtocolConfig) {
            this.securityProtocolConfig = securityProtocolConfig;
        }
    
        public JSONObject getOtherProperties() {
            return otherProperties;
        }
    
        public void setOtherProperties(JSONObject otherProperties) {
            this.otherProperties = otherProperties;
        }
    }
}
