package com.autodb.ops.dms.entity.task;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class RuJob {
    private String id;
    private int rev;
    private String type;
    private Timestamp lock_exp_time;
    private String lock_owner;

    private short exclusive;
    private String execution_id;
    private String process_instance_id;
    private String proc_def_id;

    private int retries;
    private String exception_stack_id;
    private String exception_msg;
    private Timestamp duedate;
    private String repeat;
    private String handler_type;
    private String handler_cfg;
    private String tenant_id;
}
