package com.autodb.ops.dms.domain.datasource.sql;

import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.domain.datasource.visitor.PrimaryKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SQL Service
 *
 * @author dongjs
 * @since 16/2/19
 */
public interface SQLService {
    /**
     * sql 安全脱敏
     *
     * @param sql          需要脱敏的sql
     * @param securityData 敏感数据
     * @param visitor      DatabaseVisitor
     * @param tableSet     table set
     * @return maskSql
     */
    String securityMaskSql(String sql, Map<String, Set<String>> securityData, DatabaseVisitor visitor, Set<String> tableSet);

    /**
     * execute insert sql and return rollback sql
     * @return rollback sql
     */
    String insertAndRollbackSql(Connection conn, String sql, String tableName, List<PrimaryKey> primaryKeys) throws SQLException;
}
