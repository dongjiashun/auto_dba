package com.autodb.ops.dms.service.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.entity.user.User;

import java.util.List;

/**
 * DataSourceAuth Service
 *
 * @author dongjs
 * @since 16/1/18
 */
public interface DataSourceAuthService {
    DataSourceAuth find(int id) throws AppException;

    DataSourceAuth find(int userId, int dsId) throws AppException;

    List<DataSourceAuth> findByDs(int dsId) throws AppException;

    List<DataSourceAuth> findByUserEnv(int userId, String env) throws AppException;

    List<DataSourceAuth> findByUser(int userId) throws AppException;

    int delete(List<Integer> ids) throws AppException;

    void add(DataSource dataSource, List<Integer> userIds, List<String> roles) throws AppException;

    void add(DataSource dataSource, User user, List<String> roles) throws AppException;

    void updateRoles(int id, List<String> roles) throws AppException;

    boolean hasRole(int userId, int dsId, String role) throws AppException;
}
