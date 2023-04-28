package com.autodb.ops.dms.common.util;

import com.alibaba.druid.util.JdbcUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Types;

/**
 * SqlFormat Utils
 *
 * @author dongjs
 * @since 16/1/26
 */
public final class SqlFormatUtils {
    public static String toSqlInsertFormat(Object obj, int type, String dbType) {
        if (null == obj) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        // 日期格式
        if (type == Types.TIMESTAMP) {
            if (JdbcUtils.MYSQL.equalsIgnoreCase(dbType)) {
                return "'" + obj.toString() + "'";
            } else if (JdbcUtils.SQL_SERVER.equalsIgnoreCase(dbType)) {
                sb.append("convert(datetime,'").append(obj.toString()).append("', 120)");
                return sb.toString();
            } else if (JdbcUtils.ORACLE.equalsIgnoreCase(dbType)) {
                // oracle
                sb.append("to_date('").append(obj.toString()).append("', 'yyyy-mm-dd hh24:mi:ss')");
                return sb.toString();
            }
        }

        if (type == Types.BIGINT || type == Types.DECIMAL || type == Types.BIT || type == Types.BOOLEAN
                || type == Types.DOUBLE || type == Types.FLOAT || type == Types.INTEGER || type == Types.NUMERIC
                || type == Types.SMALLINT) {
            return obj.toString();
        }

        return "'" + obj.toString().replaceAll("'", "''") + "'";
    }

    public static Object toSqlInsertTitle(String string, String dbType) {
        if (JdbcUtils.MYSQL.equalsIgnoreCase(dbType)) {
            return '`' + string + '`';
        }
        return string;
    }

    public static String toOriginalString(String string, String dbType) {
        if (JdbcUtils.MYSQL.equalsIgnoreCase(dbType)) {
            return StringUtils.strip(string, "` ");
        }
        return string;
    }

    private SqlFormatUtils() {
    }
}
