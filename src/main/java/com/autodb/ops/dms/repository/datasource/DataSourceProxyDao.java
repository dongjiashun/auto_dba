package com.autodb.ops.dms.repository.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceProxy;

import java.util.List;

/**
 * DataSource Proxy
 * @author dongjs
 * @since 16/4/20
 */
public interface DataSourceProxyDao {
    void add(DataSourceProxy dataSourceProxy) throws AppException;

    DataSourceProxy find(int id) throws AppException;

    DataSourceProxy findByNameOrHost(String name, String host) throws AppException;

    List<DataSourceProxy> findAll() throws AppException;

    boolean update(DataSourceProxy dataSourceProxy) throws AppException;

    boolean delete(int id) throws AppException;
}
