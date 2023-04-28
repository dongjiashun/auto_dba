package com.autodb.ops.dms.service.task;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.task.DataExportApply;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.user.User;
import org.activiti.engine.delegate.DelegateExecution;

import java.util.Date;

/**
 * DataExport Service
 *
 * @author dongjs
 * @since 16/1/22
 */
public interface DataExportService {
    /**
     * 0 > succcess
     * 1 > exists unknown ds
     * 2 > bad select sql
     */
    Pair<Integer, String> apply(User user, DataExportApply dataExportApply) throws AppException;

    int approve(String id, User assessor, int agree, String reason, Date execTime) throws AppException;

    /**
     * 0 > succcess
     * 1 > unknown task
     * 2 > bad select sql
     */
    Pair<Integer, String> adjust(User user, String id, boolean reApply, String reason, String sql) throws AppException;

    // TODO cancel job

    int downloadData(User user, String id) throws AppException;

    TaskData task(String definitionKey, String id) throws AppException;

    ProcessData process(String id) throws AppException;

    ProcessData processByTaskId(String definitionKey, String id) throws AppException;

    // export
    void export(DelegateExecution execution) throws AppException;

    void end(String processInstanceId) throws AppException;
}
