package com.autodb.ops.dms.repository.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.datasource.DataSourceRoleDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * DataSourceRoleDao Impl
 *
 * @author dongjs
 * @since 16/1/12
 */
@Repository
public class DataSourceRoleDaoImpl extends SuperDao implements DataSourceRoleDao {
    @Override
    public DataSourceRole findByCode(String code) throws AppException {
        return this.getSqlSession().selectOne("DataSourceRoleMapper.findByCode", code);
    }

    @Override
    public List<DataSourceRole> findAll() throws AppException {
        return this.getSqlSession().selectList("DataSourceRoleMapper.findAll");
    }

    @Override
    public Map<String, DataSourceRole> findAllCodeMap() throws AppException {
        return this.getSqlSession().selectMap("DataSourceRoleMapper.findAll", "code");
    }

    @Override
    public Map<Integer, DataSourceRole> findAllMap() throws AppException {
        return this.getSqlSession().selectMap("DataSourceRoleMapper.findAll", "id");
    }
}
