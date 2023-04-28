package com.autodb.ops.dms.common.exception;

/**
 * 异常编码
 *
 * @author dongjs
 * @since 2015/10/10
 */
public final class ExCode {
    private ExCode() {
    }

    /** 默认系统异常 */
    public static final Integer SYS_001 = 10001;
    /** 系统配置异常 */
    public static final Integer SYS_002 = 10002;
    /** 系统加密、哈希、编码异常 */
    public static final Integer SYS_003 = 10003;
    /** jackson */
    public static final Integer SYS_004 = 10004;
    /** file */
    public static final Integer SYS_005 = 10005;

    /** 默认数据库异常 */
    public static final Integer DB_001 = 11001;

    /** api */
    public static final Integer API_001 = 20001;

    /** data source 异常 */
    public static final Integer DS_001 = 30001;

    /** workflow 异常 */
    public static final Integer WF_001 = 40001;
}
