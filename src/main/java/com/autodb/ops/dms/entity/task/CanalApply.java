package com.autodb.ops.dms.entity.task;

import lombok.Data;

import java.util.Date;

/**
 * Canal Apply
 *
 * @author dongjs
 * @since 2016/11/1
 */
@Data
public class CanalApply {
    private Integer id;
    private TaskBiz task;

    private String env;
    private String sid;
    private String table;
    private String reason;

    // default client is kafka
    private String client = "kafka";
    private int manager;
    private String target;
    private int index;
    private String key;

    private String assessor;
    private Date assessTime;
    private byte assessType;
    private String assessRemark;

    public static byte toAssessType(byte type) {
        byte ret = CanalApply.AssessType.REJECT;
        switch (type) {
            case CanalApply.AssessType.AGREE:
                ret = CanalApply.AssessType.AGREE;
                break;
            case CanalApply.AssessType.MANUAL:
                ret = CanalApply.AssessType.MANUAL;
                break;
        }

        return ret;
    }

    /** assess type **/
    public static class AssessType {
        /** 手工执行 **/
        public static final byte MANUAL = 2;
        public static final byte AGREE = 1;
        public static final byte REJECT = 0;
    }
}
