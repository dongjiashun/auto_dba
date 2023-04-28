package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.entity.task.TaskBiz;
import lombok.Data;

import java.util.List;

/**
 * Process Instance Data
 *
 * @author dongjs
 * @since 16/1/15
 */
@Data
public class ProcessData {
    private String activeTask;

    private List<ActivityData> activities;

    private List<String> currUsers;

    private TaskBiz taskBiz;

    private boolean canCancel = false;
}
