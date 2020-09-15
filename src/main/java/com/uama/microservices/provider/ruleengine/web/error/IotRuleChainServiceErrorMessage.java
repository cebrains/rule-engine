package com.uama.microservices.provider.ruleengine.web.error;

import com.uama.framework.core.ErrorMessageEnum;

public enum IotRuleChainServiceErrorMessage implements ErrorMessageEnum {
	PRODUCT_ID_BLANK(10000000, "product id 为空"),
    RULE_CHAIN_NOT_EXISTS(10000001, "product id: {0}, 没有找到对应的规则链路")
    ;
	
	private Integer code;
	private String message;

	IotRuleChainServiceErrorMessage(Integer code, String message) {
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
