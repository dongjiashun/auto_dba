package com.autodb.ops.dms.service.datasource;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DataSource Service
 *
 * @author dongjs
 * @since 2015/12/29
 */
public interface DataSourceService {
    DataSource find(int id) throws AppException;

    DataSource findByEnvName(String env, String name) throws AppException;

    DataSource findByEnvSid(String env, String sid) throws AppException;

    List<DataSource> findAll() throws AppException;

    List<DataSource> findByEnv(String env) throws AppException;

    List<DataSource> findUnAuthByUserEnv(int userId, String env) throws AppException;

    int add(DataSource dataSource, boolean skipTest) throws AppException;

    int add(DataSource dataSource) throws AppException;

    int update(DataSource dataSource) throws AppException;

    Pair<Pair<Boolean, String>, Pair<Boolean, String>> testConnection(DataSource dataSource) throws AppException;

    Pair<Boolean, String> testProxyConnection(DataSource dataSource) throws AppException;

    boolean delete(int id) throws AppException;

    List<String> structNames(int id, String type) throws AppException;

    List<String> structNames(int userId, int id, String type) throws AppException;

    Map<String, Object> structInfo(int id, String type, String name) throws AppException;

    Map<String, Object> structInfo(int userId, int id, String type, String name) throws AppException;

    String showCreateTable(int dsId, String table) throws AppException;

    int syncMainPwdAsBackup(String env) throws AppException;

    Optional<String> schemaSql(String env, String sid) throws AppException;
}
