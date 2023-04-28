package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceOnline;
import com.autodb.ops.dms.entity.task.StructChange;
import com.autodb.ops.dms.entity.task.TaskBiz;

import java.util.List;

/**
 * StructChange Dao
 *
 * @author dongjs
 * @since 16/5/28
 */
public interface StructChangeDao {
    void add(StructChange structChange) throws AppException;

    StructChange findByTask(int taskId, String key) throws AppException;

    StructChange findId(int id) throws AppException;

    List<StructChange> findByTask(int taskId) throws AppException;

    List<StructChange> findByKey(String key) throws AppException;

    List<StructChange> findByOnline(String key, DataSourceOnline online, Page<StructChange> page) throws AppException;

    boolean existInProcessOnline(String key) throws AppException;

    List<TaskBiz> inProcessOnline(String key) throws AppException;

    boolean update(StructChange dataChange) throws AppException;

    /**
     * 检查是否有频繁变更
     * @param interval 单位分钟
     */
    boolean hasFrequentChange(String user, List<Integer> dsList, int interval) throws AppException;
}
