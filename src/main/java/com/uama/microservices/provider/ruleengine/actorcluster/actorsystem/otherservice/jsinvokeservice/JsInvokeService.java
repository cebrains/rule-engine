package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice;

import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.jsinvokeservice.exception.FunctionInvokeException;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.util.Map;

/**
 * @program: ulink
 * @description: js沙箱调用服务
 * @author: liwen
 * @create: 2019-05-30 09:47
 **/
public interface JsInvokeService {
    
    /**
     * 预编译
     * @param functionName 方法名称
     * @param jsScript 方法体
     * @throws ScriptException
     */
    void complie(String functionName, String jsScript) throws ScriptException;
    
    /**
     * 调用方法
     * @param functionName 方法名称
     * @param bindings 方法执行所需的变量环境
     * @return result
     * @throws FunctionInvokeException
     */
    Object invokeFunction(String functionName, Bindings bindings);
    
    /**
     * 执行某js语句
     * @param jsScript js
     * @param bindings 变量环境
     * @return result
     * @throws Exception
     */
    Object eval(String jsScript, Bindings bindings);
    
    /**
     * 删除方法
     * @param functionName 方法名称
     * @throws ScriptException
     */
    void releaseFunction(String functionName) throws ScriptException;
    
    /**
     * 生成变量环境
     * @param variableMap 需要添加的变量map
     * @return
     */
    Bindings putVariable(Map<String, Object> variableMap);
    
    /**
     * 方法是否已经预编译
     * @param functionName 方法名称
     * @return
     */
    boolean isComplied(String functionName);
}
