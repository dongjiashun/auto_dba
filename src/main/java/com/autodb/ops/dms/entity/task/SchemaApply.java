package com.autodb.ops.dms.entity.task;

import lombok.Data;

import java.util.Date;

/**
 * database schema apply
 *
 * @author dongjs
 * @since 16/7/21
 */
@Data
public class SchemaApply {
    private Integer id;
    private TaskBiz task;

    private String env;
    private String sid;

    /** 产品线 **/
    private String product;
    /** 项目场景 **/
    private String scene;
    /** 项目描述 **/
    private String productDesc;
    /** 容量规划描述 **/
    private String capacityDesc;
    /** 是否需要分表 **/
    private boolean split;
    /** 分表描述 **/
    private String splitDesc;

    private String assessor;
    private Date assessTime;
    private byte assessType;
    private String assessRemark;

    public static byte toAssessType(byte type) {
        byte ret = AssessType.REJECT;
        switch (type) {
            case AssessType.AGREE:
                ret = AssessType.AGREE;
                break;
            case AssessType.MANUAL:
                ret = AssessType.MANUAL;
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
