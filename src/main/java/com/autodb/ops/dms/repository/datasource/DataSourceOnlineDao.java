package com.autodb.ops.dms.repository.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceOnline;

/**
 * DataSourceOnline Dao
 *
 * @author dongjs
 * @since 2016/11/21
 */
public interface DataSourceOnlineDao {
    DataSourceOnline findByDataSource(int datasourceId) throws AppException;

    void add(DataSourceOnline dataSourceOnline) throws AppException;

    boolean update(DataSourceOnline dataSourceOnline) throws AppException;
}
