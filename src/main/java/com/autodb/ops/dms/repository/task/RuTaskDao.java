package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.RuJob;
import com.autodb.ops.dms.entity.task.RuTask;

import java.util.List;

public interface RuTaskDao {
    public List<RuTask> findByTaskDefKey(String task_def_key) throws AppException;
}
