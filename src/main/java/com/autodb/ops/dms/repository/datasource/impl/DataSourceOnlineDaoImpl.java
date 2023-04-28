package com.autodb.ops.dms.repository.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceOnline;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.datasource.DataSourceOnlineDao;
import org.springframework.stereotype.Repository;

/**
 * DataSourceOnlineDao Impl
 * 
 * @author dongjs
 * @since 2016/11/21
 */
@Repository
public class DataSourceOnlineDaoImpl extends SuperDao implements DataSourceOnlineDao {
    @Override
    public DataSourceOnline findByDataSource(int datasourceId) throws AppException {
        return this.getSqlSession().selectOne("DataSourceOnlineMapper.findByDataSource", datasourceId);
    }

    @Override
    public void add(DataSourceOnline dataSourceOnline) throws AppException {
        this.getSqlSession().insert("DataSourceOnlineMapper.add", dataSourceOnline);
    }

    @Override
    public boolean update(DataSourceOnline dataSourceOnline) throws AppException {
        return this.getSqlSession().update("DataSourceOnlineMapper.update", dataSourceOnline) > 0;
    }
}
