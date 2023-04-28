package com.autodb.ops.dms.entity.task;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class RuTask {
    private String id;
    private int rev;

    private String execution_id;

    private String process_instance_id;
    private String proc_def_id;

    private String name;

    private String task_def_key;
}
