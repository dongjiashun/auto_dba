package com.autodb.ops.dms.repository.datasource.impl;

import com.google.common.collect.ImmutableMap;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dongjs
 * @since 2015/12/29
 */
@Repository
public class DataSourceDaoImpl extends SuperDao implements DataSourceDao {
    @Override
    public DataSource find(int id) throws AppException {
        return this.getSqlSession().selectOne("DataSourceMapper.find", id);
    }

    @Override
    public DataSource find(String datasource) throws AppException {
        if (NumberUtils.isNumber(datasource)) {
            return this.find(NumberUtils.toInt(datasource));
        } else {
            // hack 直接name不区分环境的情况
            return this.findByEnvName(DataSource.Env.PROD, datasource);
        }
    }

    @Override
    public DataSource findByEnvName(String env, String name) throws AppException {
        return this.getSqlSession().selectOne("DataSourceMapper.findByEnvName", ImmutableMap.of("env", env, "name", name));
    }

    @Override
    public DataSource findByEnvSid(String env, String sid) throws AppException {
        return this.getSqlSession().selectOne("DataSourceMapper.findByEnvSid", ImmutableMap.of("env", env, "sid", sid));
    }

    public List<DataSource> findAllByEnvSid(String env, String sid) throws AppException{
        return this.getSqlSession().selectList("DataSourceMapper.findByEnvSid", ImmutableMap.of("env", env, "sid", sid));
    }

    @Override
    public DataSource findByUser(int userId, int id) throws AppException {
        return this.getSqlSession().selectOne("DataSourceMapper.findByUser", new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("id", id);
            }
        });
    }

    @Override
    public List<DataSource> findAll() throws AppException {
        return this.getSqlSession().selectList("DataSourceMapper.findAll");
    }

    @Override
    public List<DataSource> findByEnv(String env) throws AppException {
        return this.getSqlSession().selectList("DataSourceMapper.findByEnv", env);
    }

    @Override
    public List<DataSource> findAuthByUser(int userId) throws AppException {
        return this.getSqlSession().selectList("DataSourceMapper.findAuthByUser", userId);
    }

    @Override
    public List<DataSource> findUnAuthByUserEnv(int userId, String env) throws AppException {
        return this.getSqlSession().selectList("DataSourceMapper.findUnAuthByUserEnv", new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("env", env);
            }
        });
    }

    @Override
    public List<DataSource> findByProxy(int proxyId) throws AppException {
        return this.getSqlSession().selectList("DataSourceMapper.findByProxy", proxyId);
    }

    @Override
    public Map<Integer, DataSource> findMap(List<Integer> ids) throws AppException {
        if (ids.size() < 1) {
            return Collections.emptyMap();
        }
        return this.getSqlSession().selectMap("DataSourceMapper.findByIds", ids, "id");
    }

    @Override
    public void add(DataSource dataSource) throws AppException {
        this.getSqlSession().insert("DataSourceMapper.add", dataSource);
    }

    @Override
    public boolean update(DataSource dataSource) throws AppException {
        return this.getSqlSession().update("DataSourceMapper.update", dataSource) > 0;
    }

    @Override
    public boolean delete(int id) throws AppException {
        return this.getSqlSession().delete("DataSourceMapper.delete", id) > 0;
    }
}
