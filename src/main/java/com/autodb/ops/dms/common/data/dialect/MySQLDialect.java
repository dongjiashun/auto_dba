package com.autodb.ops.dms.common.data.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <p>
 * mysql数据库方言
 * </p>
 *
 * @author dongjs
 * @see Dialect
 * @since 2012-6-13
 */
public class MySQLDialect extends Dialect {
    @Override
    public String getLimitString(String sql, int offset, int limit) {
        sql = prepare(sql);
        StringBuilder sb = new StringBuilder(sql);
        if (offset > 0) {
            sb.append(" limit ").append(offset).append(',').append(limit);
        } else {
            sb.append(" limit ").append(limit);
        }
        return sb.toString();
    }

    @Override
    public String getLimitString(String sql, boolean hasOffset) {
        return prepare(sql) + (hasOffset ? " limit ?,?" : " limit ?");
    }

    @Override
    public void setLimitParameters(PreparedStatement ps, int parameterSize, int offset, int limit) throws SQLException {
        int index = 1;
        if (offset > 0) {
            ps.setInt(parameterSize + index, offset);
            index++;
        }
        ps.setInt(parameterSize + index, limit);
    }

    /**
     * 支持分页
     */
    @Override
    public boolean supportsLimit() {
        return true;
    }
}
