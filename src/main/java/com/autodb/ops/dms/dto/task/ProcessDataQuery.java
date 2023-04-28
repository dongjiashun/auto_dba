package com.autodb.ops.dms.dto.task;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Process Instance Data Query Condition
 *
 * @author dongjs
 * @since 16/1/27
 */
@Data
public class ProcessDataQuery {
    private String user;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date from;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date to;

    private String env;
    private String process;
    private Integer datasource;

    //任务状态
    private String taskState;

    /** -1:all 0 1 **/
    private int finished = -1;
}
