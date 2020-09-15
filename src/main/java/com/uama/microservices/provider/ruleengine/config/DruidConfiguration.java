package com.uama.microservices.provider.ruleengine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;


@Configuration
@EnableConfigurationProperties({ DataSourcePool.IotDataSource.class})
public class DruidConfiguration {

	@Bean(name = "iotDataSource", initMethod = "init", destroyMethod = "close")
	public DataSource iotDataSource(@Autowired DataSourcePool.IotDataSource iotDataSource) throws SQLException {
		return DataSourcePool.create(iotDataSource);
	}
}

