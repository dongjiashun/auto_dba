package com.autodb.ops.dms.service.task;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.domain.canal.CanalService;
import com.autodb.ops.dms.dto.task.CanalApplyAuditForm;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;

import java.util.List;

/**
 * CanalApplyService
 *
 * @author dongjs
 * @since 2016/11/1
 */
public interface CanalApplyService {
    int apply(User user, String env, Integer dsId, String table, String reason) throws AppException;

    Pair<Integer, String> approve(String id, User assessor, CanalApplyAuditForm auditForm) throws AppException;

    int adjust(User user, String id, Boolean apply, String reason) throws AppException;

    List<CanalService.Manager> managers(TaskBiz taskBiz) throws AppException;

    TaskData task(String definitionKey, String id) throws AppException;

    ProcessData process(String id) throws AppException;

    void end(String processInstanceId) throws AppException;
}
