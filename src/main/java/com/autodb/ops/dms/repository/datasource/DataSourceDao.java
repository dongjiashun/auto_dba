package com.autodb.ops.dms.repository.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSource;

import java.util.List;
import java.util.Map;

/**
 * DataSource Dao
 * @author dongjs
 * @since 2015/12/29
 */
public interface DataSourceDao {
    DataSource find(int id) throws AppException;

    /** id and name **/
    DataSource find(String datasource) throws AppException;

    DataSource findByEnvName(String env, String name) throws AppException;

    DataSource findByEnvSid(String env, String sid) throws AppException;

    List<DataSource> findAllByEnvSid(String env, String sid) throws AppException;

    DataSource findByUser(int userId, int id) throws AppException;

    List<DataSource> findAll() throws AppException;

    List<DataSource> findByEnv(String env) throws AppException;

    List<DataSource> findAuthByUser(int userId) throws AppException;

    List<DataSource> findUnAuthByUserEnv(int userId, String env) throws AppException;

    List<DataSource> findByProxy(int proxyId) throws AppException;

    Map<Integer, DataSource> findMap(List<Integer> ids) throws AppException;

    void add(DataSource dataSource) throws AppException;

    boolean update(DataSource dataSource) throws AppException;

    boolean delete(int id) throws AppException;
}
