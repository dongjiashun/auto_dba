package com.autodb.ops.dms.service.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.user.User;

import java.util.List;

/**
 * DataSourceApply Service
 *
 * @author dongjs
 * @since 16/1/14
 */
public interface DsApplyService {
    int apply(User user, String env, List<Integer> dsList, String reason) throws AppException;

    int approve(String id, User assessor, boolean agree, String reason, List<String> roles) throws AppException;

    int adjust(User user, String id, boolean reApply, String reason) throws AppException;

    TaskData task(String definitionKey, String id) throws AppException;

    ProcessData process(String id) throws AppException;

    void end(String processInstanceId) throws AppException;
}
