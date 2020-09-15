package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice.impl;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice.AbstractNashornJsInvokeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-30 11:12
 **/
@Service
public class DefaultNashornJsInvokeService extends AbstractNashornJsInvokeService {
    @Value("${js.java.sandbox_thread_pool_size:4}")
    private int sandboxThreadPoolSize;
    
    @Value("${js.java.max_cpu_time:100}")
    private long maxCpuTime;
    
    @Value("${js.java.max_errors:3}")
    private int maxErrors;
    
    @Override
    protected int getSandboxThreadPoolSize() {
        return this.sandboxThreadPoolSize;
    }
    
    @Override
    protected long getMaxCpuTime() {
        return this.maxCpuTime;
    }
    
    @Override
    protected int getMaxErrors() {
        return this.maxErrors;
    }
}
