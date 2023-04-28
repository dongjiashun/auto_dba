package com.autodb.ops.dms.service;

import com.autodb.ops.dms.service.sql.impl.SqlServiceImpl;
import org.junit.Test;

public class SqlServiceTest {
    @Test
    public void testAddLimitOrChangSize(){
        String sql = "select * from table lImit 100,1000000";
        sql = "SELECT *\n" +
                "FROM balance_log\n" +
                "LIMIT   100 ,    2000   \n";
        SqlServiceImpl sqlService = new SqlServiceImpl();
        String resultSql = sqlService.addLimitOrChangSize(sql);
        System.out.println(resultSql);
    }
}
