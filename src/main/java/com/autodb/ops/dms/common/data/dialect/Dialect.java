package com.autodb.ops.dms.common.data.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <p>
 * 数据库方言<br/>
 * 主要功能：
 * <ol>
 * <li>数据库分页</li>
 * </ol>
 * </p>
 *
 * @author dongjs
 * @since 2011-11-21
 */
public abstract class Dialect {
    /**
     * sql结束符
     */
    protected static final String SQL_END_DELIMITER = ";";

    /**
     * 该种方言表示的数据库是否支持分页
     */
    public abstract boolean supportsLimit();

    /**
     * 将sql变成分页sql语句,直接使用offset,limit的值作为占位符
     *
     * @return 包含集体值的分页sql
     */
    public abstract String getLimitString(String sql, int offset, int limit);

    /**
     * 将sql变成分页sql语句,提供将offset及limit使用占位符?替换
     *
     * @return 包含占位符的分页sql
     */
    public abstract String getLimitString(String sql, boolean hasOffset);

    /**
     * 准备sql主要是去除结束符
     */
    protected String prepare(String sql) {
        sql = sql.trim();
        if (sql.endsWith(SQL_END_DELIMITER)) {
            sql = sql.substring(0, sql.length() - 1 - SQL_END_DELIMITER.length());
        }
        return sql;
    }

    /**
     * 设置PreparedStatement参数
     *
     * @param parameterSize 预编译的sql的参数个数
     * @throws SQLException
     */
    public abstract void setLimitParameters(PreparedStatement ps, int parameterSize, int offset, int limit) throws SQLException;

    /**
     * 获取Dialect的实现，通过方言标识，如果方言不存在抛出RuntimeException
     */
    public static Dialect getInstance(String dialect) {
        if ("mysql".equalsIgnoreCase(dialect)) {
            return new MySQLDialect();
        }
        throw new RuntimeException("unknown database dialect:" + dialect);
    }
}

