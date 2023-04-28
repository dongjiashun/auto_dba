package com.autodb.ops.dms.repository.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceCobar;

/**
 * DataSourceCobar Dao
 *
 * @author dongjs
 * @since 2016/10/25
 */
public interface DataSourceCobarDao {
    DataSourceCobar findByDataSource(DataSource dataSource) throws AppException;
}
