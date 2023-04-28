package com.autodb.ops.dms.entity.task;

import lombok.Data;

import java.util.Date;

/**
 * DataChange
 *
 * @author dongjs
 * @since 16/1/25
 */
@Data
public class DataChange {
    private Integer id;
    private TaskBiz task;
    /** data source id **/
    private String key;
    private String reason;
    private String dsEnv;
    private String dsName;

    private String sql;
    private boolean executeSuccess;
    private int affectSize;
    private String message;
    private String rollbackSqlFile;
    private String backupFile;

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
