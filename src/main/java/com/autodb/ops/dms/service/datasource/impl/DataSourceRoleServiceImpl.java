package com.autodb.ops.dms.service.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;
import com.autodb.ops.dms.repository.datasource.DataSourceRoleDao;
import com.autodb.ops.dms.service.datasource.DataSourceRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DataSourceRoleService Impl
 *
 * @author dongjs
 * @since 16/1/18
 */
@Service
public class DataSourceRoleServiceImpl implements DataSourceRoleService {
    @Autowired
    private DataSourceRoleDao dataSourceRoleDao;

    @Override
    public DataSourceRole findByCode(String code) throws AppException {
        return dataSourceRoleDao.findByCode(code);
    }

    @Override
    public List<DataSourceRole> findAll() throws AppException {
        return dataSourceRoleDao.findAll();
    }
}
