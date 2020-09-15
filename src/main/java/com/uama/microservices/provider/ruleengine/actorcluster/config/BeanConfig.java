package com.uama.microservices.provider.ruleengine.actorcluster.config;

import com.uama.microservices.provider.ruleengine.actorcluster.ActorClusterContext;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice.ActorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: uama-iot
 * @description:
 * @author: liwen
 * @create: 2019-11-20 16:31
 **/
@Configuration
public class BeanConfig {
    @Bean
    public ActorClusterContext initActorClusterContext(ActorService actorService) {
        return ActorClusterContext.create(actorService.getActorSystemContext());
    }
}
