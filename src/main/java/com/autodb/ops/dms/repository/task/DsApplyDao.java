package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.DsApply;

import java.util.List;

/**
 * DsApply Dao
 *
 * @author dongjs
 * @since 16/1/18
 */
public interface DsApplyDao {
    void add(DsApply dsApply) throws AppException;

    DsApply findByTask(int taskId, String key) throws AppException;

    List<DsApply> findByTask(int taskId) throws AppException;

    boolean update(DsApply dsApply) throws AppException;
}
