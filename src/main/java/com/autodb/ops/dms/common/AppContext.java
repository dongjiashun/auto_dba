package com.autodb.ops.dms.common;

import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.security.UserDetails;
import com.autodb.ops.dms.web.exception.RedirectException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * App Context
 * @author dongjs
 * @since 2015/11/9
 */
public final class AppContext {
    private static ApplicationContext applicationContext;

    private AppContext() {
    }

    /**
     * spring ApplicationContext
     * @return spring ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        AppContext.applicationContext = applicationContext;
    }

    public static User getCurrentUser() {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null || user.getUsername() == null) {
            throw new RedirectException("/logout");
        }
        return user;
    }
}
