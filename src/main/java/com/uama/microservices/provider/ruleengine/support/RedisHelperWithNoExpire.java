package com.uama.microservices.provider.ruleengine.support;

import com.uama.framework.common.util.json.JacksonUtil;
import com.uama.framework.core.ICacheKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-13 16:03
 **/
@Component
public class RedisHelperWithNoExpire {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public void setWithNoExpire(String key, Object value, ICacheKey cacheKey) {
        set(cacheKey.getKey() + key, value);
    }
    
    public <T> T getWithNoExpire(String key, Class<T> clazz, ICacheKey cacheKey) {
        return get(cacheKey.getKey() + key, clazz);
    }
    
    private void set(String key, Object value) {
        if (Objects.nonNull(value)) {
            redisTemplate.opsForValue().set(key, JacksonUtil.objectToJson(value));
        }
    }
    
    private <T> T get(String key, Class<T> clazz) {
        String value = redisTemplate.opsForValue().get(key);
        return StringUtils.isNoneEmpty(value) ? JacksonUtil.jsonToObject(value, clazz) : null;
    }
}
