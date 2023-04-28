package com.autodb.ops.dms.dto.user;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * OperateLog Query
 *
 * @author dongjs
 * @since 16/4/27
 */
@Data
public class OperateLogQuery {
    private String env;
    private String sid;
    private String user;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date from;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date to;
}
