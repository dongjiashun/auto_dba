package com.autodb.ops.dms.domain.datasource.observer;

import com.autodb.ops.dms.entity.datasource.DataSource;
import lombok.Data;

/**
 * DataSourceChange data
 * @author dongjs
 * @since 16/4/22
 */
@Data
public class DataSourceChange {
    private final DataSource dataSource;
    private final Change change;

    public static DataSourceChange of(DataSource dataSource, Change change) {
        return new DataSourceChange(dataSource, change);
    }
}