package com.autodb.ops.dms.repository.sql.impl;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.sql.SqlHistory;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.sql.SqlHistoryDao;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * SqlHistoryDao Impl
 *
 * @author dongjs
 * @since 16/1/8
 */
@Repository
public class SqlHistoryDaoImpl extends SuperDao implements SqlHistoryDao {
    @Override
    public void add(SqlHistory sqlHistory) throws AppException {
        this.getSqlSession().insert("SqlHistoryMapper.add", sqlHistory);
    }

    @Override
    public List<SqlHistory> findByUserType(int userId, String type, Page<SqlHistory> page) throws AppException {
        HashMap<String, Object> params = new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("type", type);
            }
        };

        page.pagination.setRowCount((int) this.selectCount("SqlHistoryMapper.findByUserType", params));
        return this.getSqlSession().selectList("SqlHistoryMapper.findByUserType", params,
                new RowBounds(page.pagination.getOffset(), page.pagination.getLimit()));
    }
}
