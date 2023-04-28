package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.entity.task.TaskBiz;
import lombok.Data;

import java.util.Date;

/**
 * Task Data
 * @author dongjs
 * @since 16/1/15
 */
@Data
public class TaskData {
    private String taskId;
    private String taskKey;
    private String taskName;
    private Date taskTime;
    private Date executeTime;

    private String taskDsEnv;
    private String taskDsName;
    private TaskBiz taskBiz;
}
