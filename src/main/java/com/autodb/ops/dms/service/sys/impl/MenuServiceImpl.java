package com.autodb.ops.dms.service.sys.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.repository.sys.SysConfigDao;
import com.autodb.ops.dms.service.sys.MenuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MenuService Impl
 *
 * @author dongjs
 * @since 16/3/29
 */
@Service
public class MenuServiceImpl implements MenuService {
    private static final long LOAD_INTERVAL = 5 * 60 * 1000;
    private static final String MENUS_CONFIG_KEY = "sys.menus";
    private static final String DEFAULT_MENUS = "[]";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysConfigDao sysConfigDao;

    private volatile long lastLoadTime = 0;

    private volatile List<?> menus;

    @Override
    public String menusConfig() throws AppException {
        String value = sysConfigDao.findValue(MENUS_CONFIG_KEY);
        return StringUtils.isNotBlank(value) ? value : DEFAULT_MENUS;
    }

    @Override
    public List<?> menus() throws AppException {
        try {
            long now = System.currentTimeMillis();
            if (now - lastLoadTime > LOAD_INTERVAL) {
                synchronized (this) {
                    if (now - lastLoadTime > LOAD_INTERVAL) {
                        String content = this.menusConfig();
                        Objects.requireNonNull(content, "require menus system config");

                        this.menus = objectMapper.readValue(content, List.class);
                        this.lastLoadTime = now;
                    }
                }
            }

            return this.menus;
        } catch (IOException e) {
            throw new AppException(ExCode.SYS_004, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ?> menu(String key) throws AppException {
        List<?> menus = this.menus();
        for (Object obj : menus) {
            Map<String, Object> menu = (Map<String, Object>) obj;
            if ("menu".equals(menu.get("type")) && key.equals(menu.get("key"))) {
                return menu;
            } else if ("folder".equals(menu.get("type"))) {
                List<Map<String, Object>> subMenus = (List<Map<String, Object>>) menu.get("childes");
                if (subMenus != null) {
                    for (Map<String, Object> subMenu : subMenus) {
                        if (key.equals(subMenu.get("key"))) {
                            return subMenu;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    @Transactional
    public synchronized int update(String menus) throws AppException {
        try {
            int count = sysConfigDao.update(MENUS_CONFIG_KEY, menus);
            this.menus = objectMapper.readValue(menus, List.class);
            this.lastLoadTime = System.currentTimeMillis();
            return count;
        } catch (IOException e) {
            throw new AppException(ExCode.SYS_004, e);
        }
    }
}
