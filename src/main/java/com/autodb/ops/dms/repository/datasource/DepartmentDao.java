package com.autodb.ops.dms.repository.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.Department;

import java.util.List;

public interface DepartmentDao {

    Department find(int id) throws AppException;

    List<Department> findAll() throws AppException;

    List<Department> findByEnv(String env) throws AppException;

    void add(Department department) throws AppException;

    boolean update(Department department) throws AppException;

    boolean delete(int id) throws AppException;

    Department findByDbnameDbinstance(String dbname, String dbinstace) throws AppException;
}
