package com.autodb.ops.dms.entity.task;

import lombok.Data;

import java.util.Date;

/**
 * DataSource Apply
 *
 * @author dongjs
 * @since 16/1/18
 */
@Data
public class DsApply {
    private Integer id;
    private TaskBiz task;
    /** data source id **/
    private String key;

    private String dsEnv;
    private String dsName;

    private String reason;

    private String assessor;
    private Date assessTime;
    private byte assessType;
    private String assessRemark;

    /** assess type **/
    public static class AssessType {
        public static final byte AGREE = 1;
        public static final byte REJECT = 0;
    }
}
