package com.uama.microservices.provider.ruleengine.config;

import com.uama.microservices.provider.ruleengine.stream.channel.IotUlinkRuleEngineActorHandlerProcessor;
import com.uama.microservices.provider.ruleengine.support.DBChannelInterceptor;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.io.IOException;

@Configuration
@EnableBinding({IotUlinkRuleEngineActorHandlerProcessor.class})
@IntegrationComponentScan(value ={"com.uama.microservices.stream.*.gateway","com.uama.microservices.iot.stream.gateway"})
public class StreamConfiguration {
    @Bean
    @GlobalChannelInterceptor(patterns = "M*")
    public ChannelInterceptor globalChannelInterceptor() {
        return new DBChannelInterceptor();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() throws IOException {
        PropertySourcesPlaceholderConfigurer propertyConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertyConfigurer.setLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:/config/stream/*.properties"));
        return propertyConfigurer;
    }
}
