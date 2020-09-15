package com.uama.microservices.provider.ruleengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tk.mybatis.spring.mapper.MapperScannerConfigurer;

import java.util.Properties;

/**
 * @program: yunzhu
 * @description:
 * @author: liwen
 * @create: 2019-03-28 15:42
 **/
@Configuration
@Import({MyBatisConfiguration.class})
public class MyBatisMapperScannerConfig {
    @Bean
    public MapperScannerConfigurer iotMapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("iotSqlSessionFactory");
        mapperScannerConfigurer.setBasePackage("com.uama.microservices.provider.*.dao.iot");
        Properties properties = new Properties();
        properties.setProperty("mappers",
                "tk.mybatis.mapper.common.Mapper,tk.mybatis.mapper.common.MySqlMapper,tk.mybatis.mapper.common.IdsMapper,com.uama.framework.mapper.mybatis.CustomMapper,com.uama.framework.mapper.mybatis.UamaMapper");
        properties.setProperty("notEmpty", "false");
        properties.setProperty("IDENTITY", "select uuid()");
        properties.setProperty("ORDER", "BEFORE");
        mapperScannerConfigurer.setProperties(properties);
        return mapperScannerConfigurer;
    }
}
