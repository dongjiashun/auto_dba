package com.autodb.ops.dms.service.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.Department;

import java.util.List;

public interface DepartmentService {
    Department find(int id) throws AppException;

    List<Department> findAll() throws AppException;

    List<Department> findByEnv(String env) throws AppException;

    int add(Department dataSource) throws AppException;

    int update(Department dataSource) throws AppException;

    boolean delete(int id) throws AppException;

}
