package com.autodb.ops.dms.service.task;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.ProcessDataQuery;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.user.User;

import java.util.List;

/**
 * Task Service
 * @author dongjs
 * @since 16/1/15
 */
public interface TaskService {
    List<TaskData> userTasks(User user) throws AppException;

    int userTasksCount(User user) throws AppException;

    List<ProcessData> userProcesses(User user, Page<ProcessData> page) throws AppException;

    List<ProcessData> userRelatedProcesses(User user, ProcessDataQuery processDataQuery, Page<ProcessData> page)
            throws AppException;

    List<ProcessData> allProcesses(ProcessDataQuery processDataQuery, Page<ProcessData> page) throws AppException;

    int cancel(String startUser, String processInstanceId) throws AppException;
}
