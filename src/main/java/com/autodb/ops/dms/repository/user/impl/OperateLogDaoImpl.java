package com.autodb.ops.dms.repository.user.impl;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.user.OperateLogQuery;
import com.autodb.ops.dms.entity.user.OperateLog;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.user.OperateLogDao;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author dongjs
 * @since 16/4/26
 */
@Repository
public class OperateLogDaoImpl extends SuperDao implements OperateLogDao {
    @Override
    public void add(OperateLog log) throws AppException {
        this.getSqlSession().insert("OperateLogMapper.add", log);
    }

    @Override
    public List<OperateLog> query(OperateLogQuery query, Page<OperateLog> page) throws AppException {
        page.pagination.setRowCount((int) this.selectCount("OperateLogMapper.query", query));
        return this.getSqlSession().selectList("OperateLogMapper.query", query,
                new RowBounds(page.pagination.getOffset(), page.pagination.getLimit()));
    }
}
