package com.autodb.ops.dms.entity.datasource;

import com.autodb.ops.dms.entity.user.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * DataSource auth record
 * @author dongjs
 * @since 2015/12/29
 */
@Data
public class DataSourceAuth {
    private Integer id;
    private DataSource dataSource;
    private User user;
    private List<DataSourceRole> roles;
    private Date gmtAuth;
}
