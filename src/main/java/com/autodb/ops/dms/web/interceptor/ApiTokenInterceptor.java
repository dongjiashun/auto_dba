package com.autodb.ops.dms.web.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

/**
 * Api Token Interceptor
 *
 * @author dongjs
 * @since 2016/12/5
 */
@Component
public class ApiTokenInterceptor extends HandlerInterceptorAdapter {
    @Value("${api.token}")
    private String token;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (token.equals(request.getHeader("token")) || token.equals(request.getParameter("token"))) {
            return super.preHandle(request, response, handler);
        } else {
            response.sendError(SC_FORBIDDEN);
            return false;
        }
    }
}
