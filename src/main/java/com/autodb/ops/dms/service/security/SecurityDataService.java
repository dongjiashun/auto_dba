package com.autodb.ops.dms.service.security;

import com.autodb.ops.dms.common.exception.AppException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SecurityData Service
 *
 * @author dongjs
 * @since 16/1/28
 */
public interface SecurityDataService {
    List<Map<String, Object>> tableInfo(int dsId, String table) throws AppException;

    void update(int datasource, String table, List<String> columns) throws AppException;

    List<Map<String, Object>> securityTableInfo(int dsId, String table) throws AppException;

    List<String> securityTableList(int dsId) throws AppException;

    /**
     * 获取需要脱敏的数据，将已授权的数据排掉
     */
    Map<String, Set<String>> findMaskData(int dsId, String username) throws AppException;
}
