package com.autodb.ops.dms.service.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceProxy;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.datasource.DataSourceProxyDao;
import com.autodb.ops.dms.service.datasource.DataSourceProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * DataSourceProxyServiceImpl
 * @author dongjs
 * @since 16/4/20
 */
@Service
public class DataSourceProxyServiceImpl implements DataSourceProxyService {
    @Autowired
    private DataSourceProxyDao dataSourceProxyDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Override
    @Transactional
    public int add(DataSourceProxy proxy) throws AppException {
        DataSourceProxy dataSourceProxy = dataSourceProxyDao.findByNameOrHost(proxy.getName(), proxy.getHost());
        if (dataSourceProxy != null) {
            return 1;
        }

        proxy.setGmtCreate(new Date());
        dataSourceProxyDao.add(proxy);
        return 0;
    }

    @Override
    @Transactional
    public int update(DataSourceProxy dataSourceProxy) throws AppException {
        DataSourceProxy proxy = dataSourceProxyDao.findByNameOrHost(dataSourceProxy.getName(), dataSourceProxy.getHost());
        if (proxy != null && !proxy.getId().equals(dataSourceProxy.getId())) {
            return 1;
        }

        dataSourceProxy.setGmtModified(new Date());
        dataSourceProxyDao.update(dataSourceProxy);
        return 0;
    }

    @Override
    public DataSourceProxy find(int id) throws AppException {
        DataSourceProxy proxy = dataSourceProxyDao.find(id);
        injectDataSources(proxy);
        return proxy;
    }

    private void injectDataSources(DataSourceProxy proxy) {
        if (proxy != null) {
            proxy.setDataSources(dataSourceDao.findByProxy(proxy.getId()));
        }
    }

    @Override
    public List<DataSourceProxy> findAll() throws AppException {
        return dataSourceProxyDao.findAll();
    }

    @Override
    @Transactional
    public boolean delete(int id) throws AppException {
        List<DataSource> dataSources = dataSourceDao.findByProxy(id);
        return dataSources.size() <= 0 && this.dataSourceProxyDao.delete(id);
    }
}
