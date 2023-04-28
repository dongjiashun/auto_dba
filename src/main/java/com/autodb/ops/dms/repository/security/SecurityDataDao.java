package com.autodb.ops.dms.repository.security;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.security.SecurityData;

import java.util.List;

/**
 * SecurityData Dao
 *
 * @author dongjs
 * @since 16/1/28
 */
public interface SecurityDataDao {
    List<SecurityData> findByIds(List<Integer> ids) throws AppException;

    List<SecurityData> find(int datasource, String table) throws AppException;

    List<SecurityData> findNoAuth(int datasource, String username) throws AppException;

    List<String> findTablesByDatasource(int datasource) throws AppException;

    void add(List<SecurityData> data) throws AppException;

    int delete(int datasource, String table) throws AppException;

    int deleteAll() throws AppException;
}
