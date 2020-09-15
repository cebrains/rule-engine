package com.uama.microservices.provider.ruleengine.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.alibaba.fastjson.support.springfox.SwaggerJsonSerializer;
import com.uama.framework.common.http.ApiVersionRequestMappingHandlerMapping;
import com.uama.framework.common.util.EnvUtil;
import com.uama.microservices.provider.ruleengine.support.DBHandlerInterceptor;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import springfox.documentation.spring.web.json.Json;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		StringHttpMessageConverter stringHttpMessageConverter=new StringHttpMessageConverter();
		stringHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
		converters.add(0, stringHttpMessageConverter);
		
		FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();

		List<MediaType> supportedMediaTypes = new ArrayList<>();
		MediaType mediaTypeJson = MediaType.valueOf(MediaType.APPLICATION_JSON_UTF8_VALUE);
		supportedMediaTypes.add(mediaTypeJson);
		fastJsonHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		fastJsonConfig.getSerializeConfig().put(Json.class, new SwaggerJsonSerializer());
		fastJsonConfig.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect);
		fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);

		converters.add(1, fastJsonHttpMessageConverter);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new DBHandlerInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
		return new WebMvcRegistrations() {
			@Override
			public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
				return new ApiVersionRequestMappingHandlerMapping("v");
			}
		};
	}
	
	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor() {
	    return new MethodValidationPostProcessor();
	}
	
	@Bean
	public EnvUtil envUtil() {
		return new EnvUtil();
	}
}
