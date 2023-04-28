package com.autodb.ops.dms.service.sys;

import com.autodb.ops.dms.common.exception.AppException;

import java.util.List;
import java.util.Map;

/**
 * MenuService
 *
 * @author dongjs
 * @since 16/3/29
 */
public interface MenuService {
    String menusConfig() throws AppException;

    List<?> menus() throws AppException;

    Map<String, ?> menu(String key) throws AppException;

    int update(String menus) throws AppException;
}
