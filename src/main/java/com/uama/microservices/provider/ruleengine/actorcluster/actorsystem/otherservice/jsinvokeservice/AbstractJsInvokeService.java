package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ConstantFields;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice.exception.FunctionInvokeException;
import org.apache.commons.lang3.StringUtils;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-30 10:48
 **/
public abstract class AbstractJsInvokeService implements JsInvokeService {
    
    private static final Map<String, String> FUNCTION_NAME_MAP = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> BLACK_LISTED_FUNCTIONS = new ConcurrentHashMap<>();
    private static final String FUNCTION_PREFIX = "js_";
    private static final String REFERENCE_ERROR_JS_EXCEPTION_PREFIX = "ReferenceError";
    
    @Override
    public boolean isComplied(String functionName) {
        return AbstractJsInvokeService.FUNCTION_NAME_MAP.containsKey(functionName);
    }
    
    @Override
    public void complie(String functionName, String jsScript) throws ScriptException {
        if (StringUtils.isAnyBlank(functionName, jsScript)) {
            throw new ScriptException(String.format("Function name[%s] or js script[%s] can't be blank", functionName, jsScript));
        }
        if (AbstractJsInvokeService.FUNCTION_NAME_MAP.containsKey(functionName)) {
            throw new ScriptException(String.format("Function %s already compiled, function map: %s", functionName, FUNCTION_NAME_MAP));
        }
        String processedFunctionName = processFunctionName(functionName);
        this.doComplie(generateFunction(processedFunctionName, jsScript));
        AbstractJsInvokeService.FUNCTION_NAME_MAP.put(functionName, processedFunctionName);
    }
    
    @Override
    public Object invokeFunction(String functionName, Bindings bindings) {
        String mappedFunctionName = FUNCTION_NAME_MAP.get(functionName);
        if (StringUtils.isBlank(mappedFunctionName)) {
            throw new FunctionInvokeException(String.format("Function %s not complie yet", functionName));
        }
        if (this.isBlackListed(functionName)) {
            throw new FunctionInvokeException("Script is blacklisted due to maximum error count " + getMaxErrors() + "!");
        }
        Object result;
        try {
            result = this.doInvokeFunction(mappedFunctionName, bindings);
        } catch (FunctionInvokeException e) {
            if (!e.getMessage().startsWith(AbstractJsInvokeService.REFERENCE_ERROR_JS_EXCEPTION_PREFIX)) {
                this.onScriptExecutionError(functionName);
            }
            throw new FunctionInvokeException(e.getMessage());
        }
        return result;
    }
    
    @Override
    public void releaseFunction(String functionName) throws ScriptException {
        String mappedFunctionName = AbstractJsInvokeService.FUNCTION_NAME_MAP.remove(functionName);
        if (StringUtils.isNotBlank(mappedFunctionName)) {
            this.doReleaseFunction(mappedFunctionName);
        }
        AbstractJsInvokeService.BLACK_LISTED_FUNCTIONS.remove(functionName);
    }
    
    @Override
    public Bindings putVariable(Map<String, Object> variableMap) {
        return this.doPutVariable(variableMap);
    }
    
    @Override
    public Object eval(String jsScript, Bindings bindings) {
        return this.doEval(jsScript, bindings);
    }
    
    protected abstract Object doEval(String jsScript, Bindings bindings);
    
    protected abstract Bindings doPutVariable(Map<String, Object> variableMap);
    
    protected abstract void doComplie(String jsScript) throws ScriptException;
    
    protected abstract Object doInvokeFunction(String functionName, Bindings bindings);
    
    protected abstract void doReleaseFunction(String functionName) throws ScriptException;
    
    protected abstract int getMaxErrors();
    
    private void onScriptExecutionError(String functionName) {
        AbstractJsInvokeService.BLACK_LISTED_FUNCTIONS.computeIfAbsent(functionName, key -> new AtomicInteger(0)).incrementAndGet();
    }
    
    private boolean isBlackListed(String functionName) {
        if (AbstractJsInvokeService.BLACK_LISTED_FUNCTIONS.containsKey(functionName)) {
            AtomicInteger errorCount = AbstractJsInvokeService.BLACK_LISTED_FUNCTIONS.get(functionName);
            return errorCount.get() >= this.getMaxErrors();
        } else {
            return false;
        }
    }
    
    private String processFunctionName(String functionName) {
        return FUNCTION_PREFIX.concat(functionName.replace("-", "_"));
    }
    
    private String generateFunction(String mappedFunctionName, String jsScript) {
        return String.format(ConstantFields.JS_TEMPLATE, mappedFunctionName, jsScript);
    }
}
