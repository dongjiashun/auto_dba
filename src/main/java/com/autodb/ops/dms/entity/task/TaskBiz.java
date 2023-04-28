package com.autodb.ops.dms.entity.task;

import com.autodb.ops.dms.entity.user.User;
import lombok.Data;

import java.util.Date;

/**
 * Task Biz
 *
 * @author dongjs
 * @since 16/1/18
 */
@Data
public class TaskBiz {
    private Integer id;
    private String processInstanceId;
    private String type;
    private User startUser;

    private String status;
    private Date startTime;
    private Date endTime;
    private String info;
    private String explain;

    private Object entity;

    /** type **/
    public static class Type {
        public static final String DS_APPLY = "ds-apply";
        public static final String DATA_EXPORT = "data-export";
        public static final String DATA_CHANGE = "data-change";
        public static final String STRUCT_CHANGE = "struct-change";
        public static final String SCHEMA_APPLY = "schema-apply";
        public static final String CANAL_APPLY = "canal-apply";
    }

    /** status **/
    public static class Status {
        public static final String PROCESS = "process";
        public static final String CANCEL = "cancel";
        public static final String END = "end";
    }
}
