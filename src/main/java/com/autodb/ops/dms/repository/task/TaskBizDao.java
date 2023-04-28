package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.TaskBiz;

import java.util.List;

/**
 * TaskBiz Dao
 *
 * @author dongjs
 * @since 16/1/18
 */
public interface TaskBizDao {
    void add(TaskBiz taskBiz) throws AppException;

    boolean updateProcessInstanceId(int id, String processInstanceId) throws AppException;

    TaskBiz findByProcessInstanceId(String processInstanceId) throws AppException;

    List<TaskBiz> findByProcessInstanceIds(List<String> processInstanceIds) throws AppException;

    List<TaskBiz> findByIds(List<Integer> taskIds) throws AppException;

    int update(TaskBiz taskBiz) throws AppException;
}
