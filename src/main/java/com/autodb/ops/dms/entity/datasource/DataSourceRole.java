package com.autodb.ops.dms.entity.datasource;

import lombok.Data;

/**
 * DataSource Role
 * @author dongjs
 * @since 16/1/12
 */
@Data
public class DataSourceRole {
    private int id;
    private String code;
    private String name;
    private Integer order;
}
