package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description:
 * @author: liwen
 * @create: 2019-05-10 11:17
 **/
public class DefaultActorServiceHelper {
    private DefaultActorServiceHelper() {}
    
    /**
     * 初始化system node holder actor, 不能为空 系统必须至少初始化一个node holder actor
     * @param systemActorTypeList
     */
    public static void checkActorTypeList(Set<ActorTypeMapperEnum> systemActorTypeList) {
        if (systemActorTypeList.contains(null)) {
            throw new IllegalStateException("Need at least one actor holder to initial, add config: actor.actorsystem.actortypes");
        }
    }
    
    public static Config replaceConfig(Map<String, Object> newConfigMap, Config config) {
        if (newConfigMap.isEmpty()) {
            return config;
        }
        AtomicReference<Config> atomConfig = new AtomicReference<>(config);
        newConfigMap.forEach((key, value) -> {
            ConfigValue cv = ConfigValueFactory.fromAnyRef(value);
            atomConfig.set(atomConfig.get().withValue(key, cv));
        });
        return atomConfig.get();
    }
}
