package com.autodb.ops.dms.service.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;

import java.util.List;

/**
 * DataSourceRole Service
 * @author dongjs
 * @since 16/1/18
 */
public interface DataSourceRoleService {
    DataSourceRole findByCode(String code) throws AppException;

    List<DataSourceRole> findAll() throws AppException;
}
