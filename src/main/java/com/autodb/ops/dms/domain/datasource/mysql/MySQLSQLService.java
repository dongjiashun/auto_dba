package com.autodb.ops.dms.domain.datasource.mysql;

import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.autodb.ops.dms.common.util.SqlUtils;
import com.autodb.ops.dms.domain.datasource.sql.AbstractSQLService;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.domain.datasource.visitor.PrimaryKey;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * MySQL SQLService
 *
 * @author dongjs
 * @since 16/2/18
 */
public class MySQLSQLService extends AbstractSQLService {
    @Override
    public String getDbType() {
        return "mysql";
    }

    @Override
    protected void appendSpecial(DatabaseVisitor visitor, SQLSelect sqlselect, StringBuilder finalSql) {
        // nothing
    }

    @Override
    protected void generateSQLLeftIndividualPart(DatabaseVisitor visitor, SQLSelect sqlselect,
                                                 StringBuilder finalSQL, SQLSelectQueryBlock query) {
        // limit子句
        StringBuffer limit = new StringBuffer();
        SQLASTOutputVisitor limitVisitor = SqlUtils.createFormatOutputVisitor(limit, this.getDbType());
        MySqlSelectQueryBlock.Limit limitClause = ((MySqlSelectQueryBlock) query).getLimit();
        if (limitClause != null) {
            limitClause.accept(limitVisitor);
            finalSQL.append(" ").append(limit.toString());
        }
    }

    @Override
    public String insertAndRollbackSql(Connection conn, String sql, String tableName,
                                       List<PrimaryKey> primaryKeys) throws SQLException {
        QueryRunner runner = new QueryRunner();
        List<Long> ids = runner.insert(conn, sql, new ColumnListHandler<>());

        // rollback sql
        StringBuilder rollbackSql = new StringBuilder();
        if (ids != null && ids.size() > 0) {
            PrimaryKey primaryKey = null;
            if (primaryKeys != null) {
                for (PrimaryKey key : primaryKeys) {
                    if (key.isAutoIncrement()) {
                        primaryKey = key;
                        break;
                    }
                }
            }
            if (primaryKey != null) {
                final PrimaryKey finalPrimaryKey = primaryKey;
                ids.forEach(id -> rollbackSql.append("delete from ").append(tableName)
                        .append(" where `").append(finalPrimaryKey.getName()).append("`=").append(id).append(";\n"));
                rollbackSql.append('\n');
            }
        }

        return rollbackSql.toString();
    }
}
