package com.autodb.ops.dms.service;

import com.autodb.ops.dms.service.task.impl.StructChangeServiceImpl;
import org.junit.Test;

import java.util.List;

public class StructChangeServiceTest {
    @Test
    public void testGetTableFromSql(){
        StructChangeServiceImpl structChangeService = new StructChangeServiceImpl();
        String sql = "alter table workorder add index idx_creator_no2(creator_no);\n" +
                "alter table workorder_clone add index idx_creator_no2(creator_no);";
        List<String> names = structChangeService.getTableNameFromSql(sql);
        for(String name : names){
            System.out.println(name);
        }

        String tsql = "alter table special_scene_adjust_rule add index idx_scheduled_status_expiretime(`scheduled`,`status`,`expire_time`);";
        int type = structChangeService.type0fSql(tsql);
        System.out.println(type);
    }
}
