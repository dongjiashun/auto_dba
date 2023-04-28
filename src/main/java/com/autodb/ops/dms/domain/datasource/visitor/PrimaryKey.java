package com.autodb.ops.dms.domain.datasource.visitor;

import lombok.Data;

/**
 * PrimaryKey
 */
@Data
public class PrimaryKey {
    private String name;
    private boolean autoIncrement;
}