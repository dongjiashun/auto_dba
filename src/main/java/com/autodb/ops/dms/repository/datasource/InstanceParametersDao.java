package com.autodb.ops.dms.repository.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.InstanceParameters;

import java.util.List;

public interface InstanceParametersDao {

    InstanceParameters find(int id) throws AppException;

    List<InstanceParameters> findAll() throws AppException;

    List<InstanceParameters> findByEnv(String env) throws AppException;

    void add(InstanceParameters department) throws AppException;

    boolean update(InstanceParameters department) throws AppException;

    boolean delete(int id) throws AppException;

    InstanceParameters findByDbinstance(String dbinstace) throws AppException;
}
