package com.autodb.ops.dms.service.task;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.SchemaApplyAdjustForm;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.task.SchemaApply;
import com.autodb.ops.dms.entity.user.User;

/**
 * SchemaApply Service
 *
 * @author dongjs
 * @since 16/7/26
 */
public interface SchemaApplyService {
    int apply(User user, SchemaApply schemaApply) throws AppException;

    Pair<Integer, String> approve(String id, User assessor, byte agree, int copyDatasourceId, String dsName, String reason)
            throws AppException;

    int adjust(User user, String id, SchemaApplyAdjustForm schemaApplyAdjustForm) throws AppException;

    TaskData task(String definitionKey, String id) throws AppException;

    ProcessData process(String id) throws AppException;

    void end(String processInstanceId) throws AppException;
}
