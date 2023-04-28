package com.autodb.ops.dms.service;

import org.junit.Test;

import javax.validation.constraints.AssertTrue;

import static org.junit.Assert.*;

public class DataExportServiceTest {
    @Test
    public void testSqlSplit(){
        String sql = "select * e fRom rule limit 10;";
        String tempSql = sql.trim();
        String[] items = tempSql.substring(tempSql.toLowerCase().indexOf("from")).split("\\s");
        String tableName = items[1];
        assertEquals(tableName,"rule");
    }
}
