package com.autodb.ops.dms.entity.datasource;

import lombok.Data;

/**
 * DataSourceAuth -> DataSourceRole
 *
 * @author dongjs
 * @since 16/1/15
 */
@Data
public class DataSourceAuthRole {
    /** auth id **/
    private Integer auth;

    /** role code **/
    private String role;
}
