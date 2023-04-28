package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.CanalApply;

/**
 * CanalApply Dao
 *
 * @author dongjs
 * @since 16/11/1
 */
public interface CanalApplyDao {
    void add(CanalApply canalApply) throws AppException;

    CanalApply findByTask(int taskId) throws AppException;

    boolean update(CanalApply canalApply) throws AppException;
}
