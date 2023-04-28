package com.autodb.ops.dms.service.user.impl;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.user.OperateLogQuery;
import com.autodb.ops.dms.entity.user.OperateLog;
import com.autodb.ops.dms.repository.user.OperateLogDao;
import com.autodb.ops.dms.service.user.OperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OperateLogService Impl
 *
 * @author dongjs
 * @since 16/4/27
 */
@Service
public class OperateLogServiceImpl implements OperateLogService {
    @Autowired
    private OperateLogDao operateLogDao;

    @Override
    public List<OperateLog> query(OperateLogQuery query, Page<OperateLog> page) throws AppException {
        List<OperateLog> operateLogs = operateLogDao.query(query, page);
        page.setData(operateLogs);
        return operateLogs;
    }
}
