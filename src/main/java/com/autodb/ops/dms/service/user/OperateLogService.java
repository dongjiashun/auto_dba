package com.autodb.ops.dms.service.user;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.user.OperateLogQuery;
import com.autodb.ops.dms.entity.user.OperateLog;

import java.util.List;

/**
 * OperateLog Service
 *
 * @author dongjs
 * @since 16/4/27
 */
public interface OperateLogService {
    List<OperateLog> query(OperateLogQuery query, Page<OperateLog> page) throws AppException;
}
