package com.autodb.ops.dms.service.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceProxy;

import java.util.List;

/**
 * DataSourceProxyService
 * @author dongjs
 * @since 16/4/20
 */
public interface DataSourceProxyService {
    int add(DataSourceProxy dataSourceProxy) throws AppException;

    int update(DataSourceProxy dataSourceProxy) throws AppException;

    DataSourceProxy find(int id) throws AppException;

    List<DataSourceProxy> findAll() throws AppException;

    boolean delete(int id) throws AppException;
}
