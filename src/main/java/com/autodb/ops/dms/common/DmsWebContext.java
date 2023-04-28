package com.autodb.ops.dms.common;

import com.autodb.ops.dms.entity.user.User;

/**
 * Dms Web Context
 *
 * @author dongjs
 * @since 16/4/26
 */
public class DmsWebContext {
    private static final ThreadLocal<DmsWebContext> DMS_WEB_CONTEXTS = new ThreadLocal<>();

    private User user;
    private String username;
    //后期可以 监控用户登入ip
    private String ip = "127.0.0.1";

    public static DmsWebContext get() {
        return DMS_WEB_CONTEXTS.get();
    }

    public static void set(DmsWebContext context) {
        DMS_WEB_CONTEXTS.set(context);
    }

    public static DmsWebContext of(User user, String ip) {
        DmsWebContext context = new DmsWebContext();
        context.user = user;
        context.username = user.getUsername();
        context.ip = ip;
        return context;
    }

    public User getUser() {
        return user;
    }

    public String getUsername() {
        return username;
    }

    public String getIp() {
        return ip;
    }
}
