package com.uama.microservices.provider.ruleengine.web.error;

import com.uama.framework.core.ErrorMessageEnum;

public enum IotRuleNodeServiceErrorMessage implements ErrorMessageEnum {
	RELATION_DATA_ERROR(10000000, "规则链关系数据异常"),
    SAVE_DB_RELATION_DATA_NOT_EXISTS(10000001, "规则链SAVE DB NODE不存在"),
    CREATE_ALARM_RELATION_DATA_ERROR(10000002, "规则链CREATE ALARM NODE数量异常"),
    RULE_NODE_NOT_EXISTS(10000003, "RULE NODE 不存在"),
    KAFKA_RELATION_DATA_ERROR(10000004, "规则链KAFKA NODE数量异常"),
    CREATE_ALARM_RELATION_DATA_NOT_EXISTS(10000005, "规则链CREATE ALARM NODE不存在"),
    KAFKA_RELATION_DATA_NOT_EXISTS(10000006, "规则链KAFKA不存在")
    ;
	
	private Integer code;
	private String message;

	IotRuleNodeServiceErrorMessage(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public Integer getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
