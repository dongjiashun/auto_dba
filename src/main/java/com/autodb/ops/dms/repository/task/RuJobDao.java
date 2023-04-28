package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.RuJob;

public interface RuJobDao {
    RuJob findByExecutionId(String execution_id) throws AppException;
    boolean update(RuJob ruJob) throws AppException;
}
