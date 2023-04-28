package com.autodb.ops.dms.dto.task;

import lombok.Data;

import java.util.Date;

/**
 * Activity Data
 *
 * @author dongjs
 * @since 16/1/20
 */
@Data
public class ActivityData {
    private String name;
    private String assignee;
    private Date time;
    private String comment;
}
