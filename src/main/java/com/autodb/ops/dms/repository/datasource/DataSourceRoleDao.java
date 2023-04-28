package com.autodb.ops.dms.repository.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;

import java.util.List;
import java.util.Map;

/**
 * DataSourceRole Dao
 * @author dongjs
 * @since 2016/1/12
 */
public interface DataSourceRoleDao {
    DataSourceRole findByCode(String code) throws AppException;

    List<DataSourceRole> findAll() throws AppException;

    /** code -> **/
    Map<String, DataSourceRole> findAllCodeMap() throws AppException;

    /** id -> **/
    Map<Integer, DataSourceRole> findAllMap() throws AppException;
}
