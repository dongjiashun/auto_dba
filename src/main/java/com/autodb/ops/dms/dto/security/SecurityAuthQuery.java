package com.autodb.ops.dms.dto.security;

import lombok.Data;

/**
 * SecurityAuthQuery
 *
 * @author dongjs
 * @since 16/1/29
 */
@Data
public class SecurityAuthQuery {
    private Integer datasource;
    private String username;
}
