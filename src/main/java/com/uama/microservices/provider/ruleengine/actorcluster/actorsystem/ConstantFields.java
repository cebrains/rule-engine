package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-22 21:04
 **/
public class ConstantFields {
    private ConstantFields() {}
    
    // com.uama.microservices.provider.ruleengine.actorsystem.messages
    public static final String VALUE = "value";
    public static final String DEVICE_DATA = "DEVICE_DATA";
    public static final String END_POINT = "END_POINT";
    public static final String DEVICE_STATUS = "DEVICE_STATUS";
    
    // com.uama.microservices.provider.ruleengine.actorsystem.actor
    public static final String SYSTEM_DISPATCHER = "system-dispatcher";
    public static final String INPUT_DISPATCHER = "input-dispatcher";
    public static final String MESSAGE_TYPE_SWITCH_DISPATCHER = "messagetypeswitch-dispatcher";
    public static final String ROUTER_DISPATCHER = "router-dispatcher";
    public static final String DATA_FILTER_DISPATCHER = "datafilter-dispatcher";
    public static final String SAVE_DB_DISPATCHER = "savedb-dispatcher";
    public static final String DATA_CHECK_DISPATCHER = "datacheck-dispatcher";
    public static final String CREATE_ALARM_DISPATCHER = "createalarm-dispatcher";
    public static final String KAFKA_DISPATCHER = "kafka-dispatcher";
    
    // com.uama.microservices.provider.ruleengine.actorsystem.actor.nodeactor
    public static final String JS_SCRIPT = "jsScript";
    public static final String TRIGGER_DEVICE_IDS = "triggerDeviceIds";
    public static final String DEAD_END = "deadEnd";
    public static final String CLIENT_ID = "-RULEENGINE-";
    public static final String TOPIC_PATTERN = "default-topic";
    public static final String BOOT_STRAP_SERVERS = "localhost:9092";
    public static final String ACKS = "-1";
    public static final String SASL_MECHANISM = "PLAIN";
    public static final String SECURITY_PROTOCOL_CONFIG = "SASL_PLAINTEXT";
    
    // com.uama.microservices.provider.ruleengine.actorsystem.actor.processor.nodeprocessor
    public static final String EMPTY_JSON_ARRAY = "[]";
    public static final String END_POINT_NAME = "endpointName";
    
    // com.uama.microservices.provider.ruleengine.service.impl.rulenode
    public static final String TOPIC_PATTERN_II = "topicPattern";
    public static final String BOOTSTRAP_SERVERS = "bootstrapServers";
    public static final String OTHER_PROPERTIES = "otherProperties";
    public static final String SASL_JAAS_CONFIG = "sasl.jaas.config";
    private static final String AUTH_WORD = "password";
    public static final String SASL_JAAS_CONFIG_TEMPLATE = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" " + AUTH_WORD + "=\"%s\";";
    public static final String FILTER_FIELD_LIST = "filterFieldList";
    public static final String JS_SCRIPT_II = "return %s;";
    public static final String STATUS = "status";
    public static final String PURE_NUMBER_PREFIX = "rule_engine_pure_number_prefix_";
    
    // com.uama.microservices.provider.ruleengine.actorsystem.otherservice.jsinvokeservice
    public static final String JS_TEMPLATE = "function %s() {%s}";
    
    // com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.processor.nodeprocessor
    public static final String REFERENCE_ERROR_JS_EXCEPTION_PREFIX = "ReferenceError";
}
