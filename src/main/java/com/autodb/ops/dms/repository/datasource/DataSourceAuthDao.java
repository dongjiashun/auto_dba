package com.autodb.ops.dms.repository.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;

import java.util.List;

/**
 * DataSourceAuth Dao
 * @author dongjs
 * @since 2015/12/29
 */
public interface DataSourceAuthDao {
    DataSourceAuth find(int id) throws AppException;

    DataSourceAuth find(int userId, int dsId) throws AppException;

    List<DataSourceAuth> findByDataSource(int dataSourceId) throws AppException;

    List<DataSourceAuth> findByDataSourceRole(int dataSourceId, String role) throws AppException;

    List<DataSourceAuth> findByDataSourceRoles(int dataSourceId, List<String> roles) throws AppException;

    List<DataSourceAuth> findByUserEnv(int userId, String env) throws AppException;

    List<DataSourceAuth> findByUser(int userId) throws AppException;

    void add(DataSourceAuth dataSourceAuth) throws AppException;

    int delete(List<Integer> ids) throws AppException;

    int deleteByDataSource(int dsId) throws AppException;

    void addRoles(int id, List<String> roles) throws AppException;

    int deleteRoles(int id) throws AppException;

    boolean hasRole(int userId, int dsId, String role) throws AppException;
}
