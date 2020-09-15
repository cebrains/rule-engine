package com.uama.microservices.provider.ruleengine.support;

import com.uama.framework.core.DynamicDataSourceContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DBHandlerInterceptor extends HandlerInterceptorAdapter {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String dbkey = request.getHeader("dbkey");
		if (StringUtils.isNoneEmpty(dbkey)) {
			DynamicDataSourceContextHolder.setDataSourceType(dbkey);
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		DynamicDataSourceContextHolder.clearDataSourceType();
	}
}
