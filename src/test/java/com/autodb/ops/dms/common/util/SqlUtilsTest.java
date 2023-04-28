package com.autodb.ops.dms.common.util;

import com.autodb.ops.dms.common.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;

/**
 * SqlUtils Test
 * @author dongjs
 * @since 16/1/5
 */
public class SqlUtilsTest {
    @Test
    public void testFormat() throws Exception {
        String sql = SqlUtils.format("select * from user where id = 5; select * from user where id = 5", "mysql").getRight();
        System.out.print(sql);
    }

    @Test
    public void testDataChange() throws Exception {
        String sql = "INSERT INTO yy_test VALUES (1, 1);" +
                "UPDATE yy_test SET name = 'tt' WHERE id = 4;" +
                "DELETE FROM yy_test WHERE id = 4;" +
                "select * from user where id = 5; select * from user where id = 5";
        Triple<Boolean, List<Pair<String, String>>, String> result = SqlUtils.dataChangeStatements(sql, "mysql");

        Assert.assertTrue(result.getLeft());
        result.getMiddle().forEach(System.out::println);
    }

    @Test
    public void testRemoveDatabaseEscapeChar() {
        Assert.assertThat(SqlUtils.removeDatabaseAndEscapeChar("table"), is("table"));
        Assert.assertThat(SqlUtils.removeDatabaseAndEscapeChar("`table`"), is("table"));
        Assert.assertThat(SqlUtils.removeDatabaseAndEscapeChar("db.table"), is("table"));
        Assert.assertThat(SqlUtils.removeDatabaseAndEscapeChar("`db`.`table`"), is("table"));
    }
}