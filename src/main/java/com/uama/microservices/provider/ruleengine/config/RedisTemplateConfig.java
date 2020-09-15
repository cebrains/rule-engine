package com.uama.microservices.provider.ruleengine.config;

import com.uama.framework.common.redis.RedisHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RedisTemplateConfig {

	@Bean(name = "redisTemplate")
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, String> template = new StringRedisTemplate();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean
	public RedisHelper redisHelper(RedisConnectionFactory redisConnectionFactory) {
		RedisHelper redisHelper = new RedisHelper();
		redisHelper.setDefaultTime(1);
		redisHelper.setTimeUtil(TimeUnit.MILLISECONDS);
		redisHelper.setRedisTemplate(redisTemplate(redisConnectionFactory));
		return redisHelper;
	}
}
