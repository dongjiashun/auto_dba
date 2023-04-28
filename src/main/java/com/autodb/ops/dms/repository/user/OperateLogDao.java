package com.autodb.ops.dms.repository.user;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.user.OperateLogQuery;
import com.autodb.ops.dms.entity.user.OperateLog;

import java.util.List;

/**
 * Operate Log Dao
 *
 * @author dongjs
 * @since 16/4/26
 */
public interface OperateLogDao {
    void add(OperateLog operateLog) throws AppException;

    List<OperateLog> query(OperateLogQuery query, Page<OperateLog> page) throws AppException;
}
