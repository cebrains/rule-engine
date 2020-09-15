package com.uama.microservices.provider.ruleengine.web.error;

import com.uama.framework.core.ErrorMessageEnum;

public enum IotRuleRelationServiceErrorMessage implements ErrorMessageEnum {
	DATA_CHECK_NODE_RELATION_DATA_ERROR(10000000, "DATA CHECK NODE 关系数量异常"),
    DATA_CHECK_NODE_TO_RELATION_DATA_ERROR(10000001, "DATA CHECK NODE TO OTHER DATA CHECK NODE 关系数量异常"),
    DATA_CHECK_NODE_NOT_EXISTS(10000002, "DATA CHECK NODE 不存在 或者 不为DATA CHECK 类型")
    ;
	
	private Integer code;
	private String message;

	IotRuleRelationServiceErrorMessage(Integer code, String message) {
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
