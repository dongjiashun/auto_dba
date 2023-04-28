package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.DataChange;

import java.util.List;

/**
 * DataChange Dao
 *
 * @author dongjs
 * @since 16/1/25
 */
public interface DataChangeDao {
    void add(DataChange dataChange) throws AppException;

    DataChange findByTask(int taskId, String key) throws AppException;

    List<DataChange> findByTask(int taskId) throws AppException;

    boolean update(DataChange dataChange) throws AppException;
}
