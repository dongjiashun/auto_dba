package com.autodb.ops.dms.service.sys.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.repository.sys.SysConfigDao;
import com.autodb.ops.dms.service.sys.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * SysConfigService Impl
 *
 * @author dongjs
 * @since 16/7/23
 */
@Service
public class SysConfigServiceImpl implements SysConfigService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysConfigDao sysConfigDao;

    @Override
    public String findValue(String key) throws AppException {
        return sysConfigDao.findValue(key);
    }

    @Override
    public List<String> findListValue(String key) throws AppException {
        try {
            String value = this.findValue(key);
            return objectMapper.readValue(value, new TypeReference<List<String>>() { });
        } catch (IOException e) {
            throw new AppException(ExCode.SYS_004, e);
        }
    }
}
