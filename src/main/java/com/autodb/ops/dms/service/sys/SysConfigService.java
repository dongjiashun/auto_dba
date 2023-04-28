package com.autodb.ops.dms.service.sys;

import com.autodb.ops.dms.common.exception.AppException;

import java.util.List;

/**
 * SysConfig Service
 *
 * @author dongjs
 * @since 16/7/23
 */
public interface SysConfigService {
    String findValue(String key) throws AppException;

    List<String> findListValue(String key) throws AppException;
}
