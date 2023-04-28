package com.autodb.ops.dms.repository.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceProxy;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.datasource.DataSourceProxyDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * DataSourceProxy Impl
 *
 * @author dongjs
 * @since 16/4/20
 */
@Repository
public class DataSourceProxyDaoImpl extends SuperDao implements DataSourceProxyDao {
    @Override
    public void add(DataSourceProxy dataSourceProxy) throws AppException {
        this.getSqlSession().insert("DataSourceProxyMapper.add", dataSourceProxy);
    }

    @Override
    public DataSourceProxy find(int id) throws AppException {
        return this.getSqlSession().selectOne("DataSourceProxyMapper.find", id);
    }

    @Override
    public DataSourceProxy findByNameOrHost(String name, String host) throws AppException {
        return this.getSqlSession().selectOne("DataSourceProxyMapper.findByNameOrHost", new HashMap<String, Object>() {
            {
                put("name", name);
                put("host", host);
            }
        });
    }

    @Override
    public List<DataSourceProxy> findAll() throws AppException {
        return this.getSqlSession().selectList("DataSourceProxyMapper.findAll");
    }

    @Override
    public boolean update(DataSourceProxy dataSourceProxy) throws AppException {
        return this.getSqlSession().update("DataSourceProxyMapper.update", dataSourceProxy) > 0;
    }

    @Override
    public boolean delete(int id) throws AppException {
        return this.getSqlSession().delete("DataSourceProxyMapper.delete", id) > 0;
    }
}
