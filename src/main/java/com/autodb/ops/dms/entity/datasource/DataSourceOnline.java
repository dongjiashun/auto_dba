package com.autodb.ops.dms.entity.datasource;

import lombok.Data;

import java.util.Date;

/**
 * DataSourceOnline
 *
 * @author dongjs
 * @since 2016/11/10
 */
@Data
public class DataSourceOnline {
    private Integer id;
    private DataSource dataSource;

    private Integer lastChangeId;
    private Date lastChangeTime;

    private Integer onlineChangeId;
    private Date onlineTime;
}
