package com.uama.microservices.provider.ruleengine.config;

import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class MyBatisConfiguration {
	@Bean(name = "iotSqlSessionFactory")
	public SqlSessionFactory iotSqlSessionFactory(@Qualifier("iotDataSource")DataSource iotDataSource) throws Exception {
		SqlSessionFactoryBean sfb = new SqlSessionFactoryBean();
		sfb.setDataSource(iotDataSource);
		sfb.setTypeAliasesPackage("com.uama.microservices.provider.*.model");
		org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
		config.setLogImpl(Slf4jImpl.class);
		sfb.setConfiguration(config);

		// 分页插件
		PageInterceptor pageInterceptor = new PageInterceptor();
		Properties properties = new Properties();
		properties.setProperty("reasonable", "false");
		properties.setProperty("helperDialect", "mysql");
		properties.setProperty("supportMethodsArguments", "true");
		properties.setProperty("returnPageInfo", "check");
		properties.setProperty("params", "count=countSql");
		properties.setProperty("autoRuntimeDialect", "true");
		pageInterceptor.setProperties(properties);

		// 添加插件
		sfb.setPlugins(new Interceptor[] { pageInterceptor });

		return sfb.getObject();
	}

	@Bean(name = "iotSqlSessionTemplate")
	public SqlSessionTemplate iotSqlSessionTemplate(
			@Qualifier("iotSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
	
    @Bean(name = "iotDataSourceTransactionManager")
    public DataSourceTransactionManager iotDSTM(@Qualifier("iotDataSource")DataSource iotDataSource) {
        DataSourceTransactionManager dstm = new DataSourceTransactionManager(iotDataSource);
        dstm.setNestedTransactionAllowed(true);
        return dstm;
    }
}