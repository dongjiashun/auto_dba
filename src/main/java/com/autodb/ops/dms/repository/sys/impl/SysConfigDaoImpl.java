package com.autodb.ops.dms.repository.sys.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.sys.SysConfigDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

/**
 * SysConfigDao Impl
 *
 * @author dongjs
 * @since 16/3/29
 */
@Repository
public class SysConfigDaoImpl extends SuperDao implements SysConfigDao {
    @Override
    public String findValue(String key) throws AppException {
        return this.getSqlSession().selectOne("SysConfigMapper.findValue", key);
    }

    @Override
    public int update(String key, String value) throws AppException {
        return this.getSqlSession().update("SysConfigMapper.update", new HashMap<String, Object>() {
            {
                put("key", key);
                put("value", value);
            }
        });
    }
}
