package com.uama.microservices.provider.ruleengine.cache;

import com.uama.framework.core.ICacheKey;
import com.uama.microservices.provider.ruleengine.support.SystemConstant;

import java.util.concurrent.TimeUnit;

public enum RuleChainCacheKey implements ICacheKey {
	PRODUCT_ID_MAP_RULE_CHAIN("productid.rulechain", 0, TimeUnit.SECONDS);
	
	private String key;
	private int timeout;
	private TimeUnit unit;

	RuleChainCacheKey(String key, int timeout, TimeUnit unit) {
		this.key = key;
		this.timeout = timeout;
		this.unit = unit;
	}

	@Override
	public String getKey() {
		return SystemConstant.CACHEKEY_PREFIX + SEPARATE + key + SEPARATE;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public TimeUnit getUnit() {
		return unit;
	}

}
