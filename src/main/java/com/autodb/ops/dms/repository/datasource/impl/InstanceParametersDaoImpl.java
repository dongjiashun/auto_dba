package com.autodb.ops.dms.repository.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.InstanceParameters;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.datasource.InstanceParametersDao;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InstanceParametersDaoImpl extends SuperDao implements InstanceParametersDao {
    @Override
    public InstanceParameters find(int id) throws AppException {
        return this.getSqlSession().selectOne("InstanceParametersMapper.find", id);
    }

    @Override
    public List<InstanceParameters> findAll() throws AppException {
        return this.getSqlSession().selectList("InstanceParametersMapper.findAll");
    }

    @Override
    public List<InstanceParameters> findByEnv(String env) throws AppException {
        return this.getSqlSession().selectList("InstanceParametersMapper.findByEnv", env);
    }

    @Override
    public void add(InstanceParameters instanceParameters) throws AppException {
        this.getSqlSession().insert("InstanceParametersMapper.add", instanceParameters);
    }

    @Override
    public boolean update(InstanceParameters instanceParameters) throws AppException {
        return this.getSqlSession().update("InstanceParametersMapper.update", instanceParameters) > 0;
    }

    @Override
    public boolean delete(int id) throws AppException {
        return this.getSqlSession().delete("InstanceParametersMapper.delete", id) > 0;
    }

    @Override
    public InstanceParameters findByDbinstance(String dbinstace) throws AppException {
        return this.getSqlSession().selectOne("InstanceParametersMapper.findByDbinstance", ImmutableMap.of("dbinstace", dbinstace));
    }
}
