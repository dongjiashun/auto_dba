package com.autodb.ops.dms.web.interceptor;

import com.autodb.ops.dms.common.DmsWebContext;
import com.autodb.ops.dms.common.util.ServletUtils;
import com.autodb.ops.dms.security.UserDetails;
//import com.autodb.pt.druid.masking.common.util.UserContextUtil;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DmsWebContext Filter
 *
 * @author dongjs
 * @since 16/4/26
 */
@Component
public class DmsWebContextInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            UserDetails user = (UserDetails) context.getAuthentication().getPrincipal();
            if (user != null) {
                DmsWebContext.set(DmsWebContext.of(user, ServletUtils.getRemoteAddr(request)));
//                UserContextUtil.setLocalUser(String.valueOf(user.getId()), user.getUsername());
            }
        }

    	
        return true;
    }
}
