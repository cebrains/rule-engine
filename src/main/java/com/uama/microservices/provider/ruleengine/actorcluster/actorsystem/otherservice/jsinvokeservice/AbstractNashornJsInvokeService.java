package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice.exception.FunctionInvokeException;
import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-30 11:01
 **/
@Slf4j
public abstract class AbstractNashornJsInvokeService extends AbstractJsInvokeService {
    
    private NashornSandbox sandbox;
    private ExecutorService sandboxExecutorService;
    private final ScriptContext scriptContext = new SimpleScriptContext();
    
    @PostConstruct
    public void init() {
        this.sandbox = NashornSandboxes.create();
        this.sandboxExecutorService = Executors.newWorkStealingPool(getSandboxThreadPoolSize());
        this.sandbox.setExecutor(this.sandboxExecutorService);
        this.sandbox.setMaxCPUTime(getMaxCpuTime());
        this.sandbox.allowNoBraces(false);
        this.sandbox.allowLoadFunctions(true);
        this.sandbox.setMaxPreparedStatements(30);
    }
    
    @PreDestroy
    public void stop() {
        if (null != this.sandboxExecutorService) {
            this.sandboxExecutorService.shutdownNow();
        }
    }
    
    protected abstract int getSandboxThreadPoolSize();
    
    protected abstract long getMaxCpuTime();
    
    @Override
    protected abstract int getMaxErrors();
    
    @Override
    protected Bindings doPutVariable(Map<String, Object> variableMap) {
        Bindings bindings = this.sandbox.createBindings();
        bindings.putAll(variableMap);
        return bindings;
    }
    
    @Override
    protected synchronized void doComplie(String jsScript) throws ScriptException {
        this.sandbox.eval(jsScript, this.scriptContext);
    }
    
    @Override
    protected Object doInvokeFunction(String functionName, Bindings bindings) {
        return this.doEval(functionName + "()", bindings);
    }
    
    @Override
    protected synchronized Object doEval(String jsScript, Bindings bindings) {
        this.scriptContext.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        try {
            return this.sandbox.eval(jsScript, this.scriptContext);
        } catch (Exception e) {
            throw new FunctionInvokeException(e.getMessage());
        }
    }
    
    @Override
    protected synchronized void doReleaseFunction(String functionName) throws ScriptException {
        this.sandbox.eval(functionName + " = undefined;", this.scriptContext);
    }
}
