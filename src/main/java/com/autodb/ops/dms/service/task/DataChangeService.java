package com.autodb.ops.dms.service.task;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.task.DataChangeApply;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.user.User;

/**
 * DataChange Service
 *
 * @author dongjs
 * @since 16/1/25
 */
public interface DataChangeService {
    /**
     * 0 > succcess
     * 1 > exists unknown ds
     * 2 > bad insert or update or delete sql
     */
    Pair<Integer, String> apply(User user, DataChangeApply dataChangeApply) throws AppException;

    Pair<Integer, String> approve(String id, User assessor, boolean agree, boolean backup, String reason) throws AppException;

    /**
     * 0 > succcess
     * 1 > unknown task
     * 2 > bad select sql
     */
    Pair<Integer, String> adjust(User user, String id, boolean reApply, String reason, String sql) throws AppException;

    int result(User user, String id) throws AppException;

    TaskData task(String definitionKey, String id) throws AppException;

    ProcessData process(String id) throws AppException;

    ProcessData processByTaskId(String definitionKey, String id) throws AppException;

    void end(String processInstanceId) throws AppException;
}
