package com.autodb.ops.dms.service.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.InstanceParameters;

import java.util.List;

public interface InstanceParametersService {
    InstanceParameters find(int id) throws AppException;

    List<InstanceParameters> findAll() throws AppException;

    List<InstanceParameters> findByEnv(String env) throws AppException;

    int add(InstanceParameters instanceParameters) throws AppException;

    int update(InstanceParameters instanceParameters) throws AppException;

    boolean delete(int id) throws AppException;
}
