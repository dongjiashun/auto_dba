package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.SchemaApply;

/**
 * SchemaApply Dao
 *
 * @author dongjs
 * @since 16/7/26
 */
public interface SchemaApplyDao {
    void add(SchemaApply schemaApply) throws AppException;

    SchemaApply findByTask(int taskId) throws AppException;

    boolean update(SchemaApply schemaApply) throws AppException;
}
