package com.autodb.ops.dms.entity.security;

import com.autodb.ops.dms.entity.datasource.DataSource;
import lombok.Data;

import java.util.Date;

/**
 * Security Data
 *
 * @author dongjs
 * @since 16/1/28
 */
@Data
public class SecurityData {
    private int id;
    private DataSource dataSource;
    private String table;
    private String column;
    private Date gmtCreate;
}
