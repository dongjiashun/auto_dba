package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.entity.task.StructChange;
import com.autodb.ops.dms.entity.task.TaskBiz;
import lombok.Data;

import java.util.Date;


/**
 * Simple StructChange
 *
 * @author dongjs
 * @since 16/11/14
 */
@Data
public class SimpleStructChange {
    private Integer id;
    private String processInstanceId;
    private String title;
    private Date startTime;
    private Date executeTime;
    private String startUser;

    public static SimpleStructChange of(StructChange structChange) {
        TaskBiz task = structChange.getTask();
        SimpleStructChange change = new SimpleStructChange();
        change.id = structChange.getId();
        change.processInstanceId = task.getProcessInstanceId();
        change.title = task.getExplain();
        change.startTime = task.getStartTime();
        change.executeTime = structChange.getExecuteTime();
        change.startUser = task.getStartUser().getUsername();
        return change;
    }
}