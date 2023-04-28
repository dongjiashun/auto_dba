package com.autodb.ops.dms.entity.sql;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.user.User;
import lombok.Data;

import java.util.Date;

/**
 * Sql History
 *
 * @author dongjs
 * @since 16/1/8
 */
@Data
public class SqlHistory {
    private Integer id;
    private DataSource dataSource;
    private User user;

    private String type;
    private String sql;
    private String execSql;
    private String execHash;
    private int execTime;
    private int count;

    private Date gmtCreate;

    public void setExecSql(String execSql) {
        this.execSql = execSql;
        this.execHash = hash(execSql);
    }

    public static String hash(String execSql) {
        return Hashing.md5().hashString(execSql, Charsets.UTF_8).toString();
    }

    /** sql history type **/
    public static final class Type {
        public static final String SELECT = "SELECT";
    }
}
