package com.autodb.ops.dms.service.task;


public enum TaskDefinitionKeys {
    APPLY("apply","申请"),
    ADJUST("adjust","调整"),
    AUDIT("audit","审核"),
    STRUCTCHANGE("structChange","执行结构变更"),
    WAINTING("waiting","等待执行"),
    DOWNLOAD("downloadData","下载数据"),
    RESULT("result","查看结果");

    String name;
    String desc;

    TaskDefinitionKeys(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }
}
