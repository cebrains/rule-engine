package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-30 11:41
 **/
@Component
public class SpringContextUtil {
    
    private static ApplicationContext applicationContext;
    
    @Autowired
    SpringContextUtil(ApplicationContext applicationContext) {
        setStaticApplicationContext(applicationContext);
    }
    
    private static void setStaticApplicationContext(ApplicationContext applicationContext) {
        SpringContextUtil.applicationContext = applicationContext;
    }
    
    public static Object getBean(String name){
        return SpringContextUtil.applicationContext.getBean(name);
    }
    
    public static <T> T getBean(Class<T> clazz){
        return SpringContextUtil.applicationContext.getBean(clazz);
    }
    
    public static <T> T getBean(String name,Class<T> clazz){
        return SpringContextUtil.applicationContext.getBean(name, clazz);
    }
    
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}

