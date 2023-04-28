package com.autodb.ops.dms.repository.sys;

import com.autodb.ops.dms.common.exception.AppException;

/**
 * SysConfig Dao
 *
 * @author dongjs
 * @since 16/3/29
 */
public interface SysConfigDao {
    String findValue(String key) throws AppException;

    int update(String key, String value) throws AppException;
}
