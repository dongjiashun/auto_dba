package com.autodb.ops.dms.repository.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceCobar;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.datasource.DataSourceCobarDao;
import org.springframework.stereotype.Repository;

/**
 * DataSourceCobarDaoImpl
 * @author dongjs
 * @since 2016/10/25
 */
@Repository
public class DataSourceCobarDaoImpl extends SuperDao implements DataSourceCobarDao {
    @Override
    public DataSourceCobar findByDataSource(DataSource dataSource) throws AppException {
        DataSourceCobar cobar = getSqlSession().selectOne("DataSourceCobarMapper.findByDataSource", dataSource.getId());//fixme 这里会把cobar datasource的sid赋值给datasource,目前这个值没有用处，后续考虑把这个字段去掉
        if (cobar != null) {
            cobar.setDataSource(dataSource);
        }
        return cobar;
    }
}
