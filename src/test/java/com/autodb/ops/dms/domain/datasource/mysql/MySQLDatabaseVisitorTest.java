package com.autodb.ops.dms.domain.datasource.mysql;

import com.autodb.ops.dms.domain.datasource.visitor.ConnectionInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * MySQLDatabaseVisitor Test
 * @author dongjs
 * @since 16/7/18
 */
public class MySQLDatabaseVisitorTest {
    private MySQLDatabaseVisitor visitor;

    @Before
    public void setUp() throws Exception {
        visitor = new MySQLDatabaseVisitor();
        visitor.init(new ConnectionInfo("mysql", "127.0.0.1", 3306, "dms", "root", "123456"));
    }

    @After
    public void tearDown() throws Exception {
        visitor.close();
    }

    @Test
    public void testGetVariables() throws Exception {
        System.out.println(visitor.getVariables("%timeout%"));
    }

    @Test
    public void testGetVariable() throws Exception {
        System.out.println(visitor.getVariable("read_only"));
    }

    @Test
    public void testCreateDatabase() throws Exception {
        int update = visitor.update("CREATE DATABASE IF NOT EXISTS test2 DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci");
        System.out.println(update);
    }
}