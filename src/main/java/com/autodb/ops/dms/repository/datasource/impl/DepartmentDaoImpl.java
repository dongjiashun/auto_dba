package com.autodb.ops.dms.repository.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.Department;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.datasource.DepartmentDao;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DepartmentDaoImpl extends SuperDao implements DepartmentDao{

    @Override
    public Department find(int id) throws AppException {
        return this.getSqlSession().selectOne("DepartmentMapper.find", id);
    }

    @Override
    public List<Department> findAll() throws AppException {
        return this.getSqlSession().selectList("DepartmentMapper.findAll");
    }

    @Override
    public List<Department> findByEnv(String env) throws AppException {
        return this.getSqlSession().selectList("DepartmentMapper.findByEnv", env);
    }

    @Override
    public void add(Department department) throws AppException {
        this.getSqlSession().insert("DepartmentMapper.add", department);
    }

    @Override
    public boolean update(Department department) throws AppException {
        return this.getSqlSession().update("DepartmentMapper.update", department) > 0;
    }

    @Override
    public boolean delete(int id) throws AppException {
        return this.getSqlSession().delete("DepartmentMapper.delete", id) > 0;
    }

    @Override
    public Department findByDbnameDbinstance(String dbname, String dbinstace) throws AppException {
        return this.getSqlSession().selectOne("DepartmentMapper.findByDbnameDbinstance", ImmutableMap.of("dbname", dbname, "dbinstace", dbinstace));
    }

}
