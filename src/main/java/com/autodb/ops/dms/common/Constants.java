package com.autodb.ops.dms.common;

/**
 * Constants
 *
 * @author dongjs
 * @since 16/2/29
 */
public final class Constants {
    /** sql长度限制 **/
    public static final int SQL_MAX_SIZE = 65535;

    /** role > owner **/
    public static final String ROLE_OWNER = "owner";

    /** hidden prefix **/
    public static final String HIDDEN_PREFIX = "h_h_";

    public static final String ONLINE_FLAG = "[上线] ";
    public static final String ONLINE_TITLE = "日常上线";

    public static final String STASH_FLAG = "[暂存提交] ";
    public static final String STASH_TITLE = "日常上线";

    private Constants() {
    }
}
