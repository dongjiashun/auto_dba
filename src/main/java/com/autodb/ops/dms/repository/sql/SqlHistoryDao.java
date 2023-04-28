package com.autodb.ops.dms.repository.sql;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.sql.SqlHistory;

import java.util.List;

/**
 * SqlHistory Dao
 *
 * @author dongjs
 * @since 16/1/8
 */
public interface SqlHistoryDao {
    void add(SqlHistory sqlHistory) throws AppException;

    List<SqlHistory> findByUserType(int userId, String type, Page<SqlHistory> page) throws AppException;
}
