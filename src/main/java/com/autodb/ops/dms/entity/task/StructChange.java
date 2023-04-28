package com.autodb.ops.dms.entity.task;

import lombok.Data;

import java.util.Date;

/**
 * Struct Change
 *
 * @author dongjs
 * @since 16/5/28
 */
@Data
public class StructChange {
    private Integer id;
    private TaskBiz task;
    /** data source id **/
    private String key;
    private String reason;
    private String dsEnv;
    private String dsName;

    private byte changeType;
    private String sql;
    /** 参照信息 **/
    private String reference;

    private String executor;
    private Date executeTime;
    private byte executeType;
    private String executeRemark;
    private int executeStatus;
    private String executeHash;

    private String assessor;
    private Date assessTime;
    private byte assessType;
    private String assessRemark;

    private boolean online;
    private Integer lastChangeId;
    private Date lastChangeTime;

    public static byte toExecuteType(byte type) {
        byte ret = ExecuteType.REJECT;
        switch (type) {
            case ExecuteType.AGREE:
                ret = ExecuteType.AGREE;
                break;
            case ExecuteType.MANUAL:
                ret = ExecuteType.MANUAL;
                break;
        }

        return ret;
    }

    /** change type **/
    public static class ChangeType {
        public static final byte CREATE = 0;
        public static final byte MIXED = 1;
    }

    /** assess type **/
    public static class AssessType {
        public static final byte AGREE = 1;
        public static final byte REJECT = 0;
    }

    /** execute type **/
    public static class ExecuteType {
        /** 手工执行 **/
        public static final byte MANUAL = 2;
        public static final byte AGREE = 1;
        public static final byte REJECT = 0;
    }

    /** execute status **/
    public static class ExecuteStatus {
        public static final byte INIT = 0;
        public static final byte RUNNING = 1;
        public static final byte SUCCESS = 2;
        public static final byte FAIL = 3;
        public static final byte TIMEOUT = 4;
        public static final byte ABORTED = 5;
    }
}
