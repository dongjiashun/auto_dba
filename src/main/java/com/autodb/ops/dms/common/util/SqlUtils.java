package com.autodb.ops.dms.common.util;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleOutputVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelectQueryBlock;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerTop;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerOutputVisitor;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.util.JdbcUtils;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.entity.task.StructChange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SQL utils
 *
 * @author dongjs
 * @since 16/1/5
 */
public final class SqlUtils {
    public static final String INSERT = "insert";
    public static final String DELETE = "delete";
    public static final String UPDATE = "update";
    public static final String SELECT = "select";

    public static final String CREATE = "create";
    public static final String ALTER = "alter";
    public static final String DROP = "drop";
    public static final String RENAME = "rename";
    public static final String TRUNCATE = "truncate";

    public static final String DESC = "describe";
    public static final String SHOW_CREATE_TABLE = "showcreatetable";
    public static final String SHOW_TABLES = "showtables";

    public static final int NAME_LENGTH = 30;

    private SqlUtils() {
    }

    /**
     * format sql
     * @param sql sql string
     * @param type db type
     * @return pretty sql, exception return false -> message
     */
    public static Pair<Boolean, String> format(String sql, String type) {
        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, type);
            List<SQLStatement> statementList = parser.parseStatementList();

            StringBuilder out = new StringBuilder();
            SQLASTOutputVisitor visitor = SQLUtils.createFormatOutputVisitor(out, statementList, type);

            for (int i = 0; i < statementList.size(); i++) {
                SQLStatement stmt = statementList.get(i);
                stmt.accept(visitor);
                String str = out.toString().trim();
                if (!str.endsWith(";\n") && !str.endsWith(";")) {
                    out.append(";\n");
                }

                if (i < statementList.size() - 1) {
                    out.append("\n");
                }
            }

            return Pair.of(true, out.toString());
        } catch (Exception e) {
            return Pair.of(false, e.getMessage());
        }
    }

    /**
     * select statement
     * @param sql sql string
     * @param type db type
     * @return select statement, exception return false -> [message]
     */
    public static Pair<Boolean, String> selectStatement(String sql, String type) {
        try {
            List<String> selects = getSelectStatements(sql, type);
            if (selects.size() > 0) {
                return Pair.of(true, selects.get(0));
            } else {
                return Pair.of(false, "must provide at least one select statement");
            }
        } catch (Exception e) {
            return Pair.of(false, e.getMessage());
        }
    }

    /**
     * select statements
     * @param sql sql string
     * @param type db type
     * @return select statements, exception return false -> [message]
     */
    public static Pair<Boolean, List<String>> selectStatements(String sql, String type) {
        try {
            List<String> selects = getSelectStatements(sql, type);
            if (selects.size() > 0) {
                return Pair.of(true, selects);
            } else {
                return Pair.of(false, Collections.singletonList("must provide at least one select statement"));
            }
        } catch (Exception e) {
            return Pair.of(false, Collections.singletonList(e.getMessage()));
        }
    }

    /**
     * query statement
     * @param sql sql string
     * @param type db type
     * @return query statement, exception return false -> [message]
     */
    public static Pair<Boolean, String> queryStatement(String sql, String type) {
        try {
            List<String> queryStatements = getQueryStatements(sql, type);
            if (queryStatements.size() > 0) {
                return Pair.of(true, queryStatements.get(0));
            } else {
                return Pair.of(false, "must provide at least one query statement");
            }
        } catch (Exception e) {
            return Pair.of(false, e.getMessage());
        }
    }

    /**
     * query statements
     * @param sql sql string
     * @param type db type
     * @return query statements, exception return false -> [message]
     */
    public static Pair<Boolean, List<String>> queryStatements(String sql, String type) {
        try {
            List<String> queryStatements = getQueryStatements(sql, type);
            if (queryStatements.size() > 0) {
                return Pair.of(true, queryStatements);
            } else {
                return Pair.of(false, Collections.singletonList("must provide at least one query statement"));
            }
        } catch (Exception e) {
            return Pair.of(false, Collections.singletonList(e.getMessage()));
        }
    }

    /**
     * insert or update statements
     * @param sql sql string
     * @param type db type
     * @return right, insert or update statements, exception return false -> [message]
     */
    public static Triple<Boolean, List<Pair<String, String>>, String> dataChangeStatements(String sql, String type) {
        try {
            List<Pair<String, String>> selects = getDataChangeStatements(sql, type);
            if (selects.size() > 0) {
                return Triple.of(true, selects, "");
            } else {
                return Triple.of(false, null, "must provide at least one insert or update or delete statement");
            }
        } catch (Exception e) {
            return Triple.of(false, null, e.getMessage());
        }
    }

    /**
     * insert or update statements
     * @param type change type
     * @param sql sql string
     * @param dbType db type
     * @return right, insert or update statements, exception return false -> [message]
     */
    public static Triple<Boolean, List<String>, String> structChangeStatements(byte type, String sql, String dbType) {
        try {
            List<String> statements = getStructChangeStatements(type, sql, dbType);
            if (statements.size() > 0) {
                return Triple.of(true, statements, "");
            } else {
                return Triple.of(false, null, "must provide at least one DDL statement");
            }
        } catch (Exception e) {
            return Triple.of(false, null, e.getMessage());
        }
    }

    /**
     * insert or update statements
     * @param sql sql string
     * @param dbType db type
     * @return right, insert or update statements, exception return false -> [message]
     */
    public static Triple<Boolean, List<String>, String> structChangeStatements(String sql, String dbType) {
        return structChangeStatements(StructChange.ChangeType.MIXED, sql, dbType);
    }

    /**
     * 判断是否是拷贝插入
     *
     * @param dbType db type
     * @param sql sql
     * @param selectClauseBuffer select部分
     * @param selectTableBuffer 表名
     * @return 是 true 否则 false
     */
    public static boolean isCopyInsert(String dbType, String sql, StringBuffer selectClauseBuffer, StringBuffer selectTableBuffer) {
        if (dbType != null && sql != null) {
            // parser得到AST
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
            List<SQLStatement> stmtList = parser.parseStatementList();
            if (stmtList != null && stmtList.size() == 1) {
                SQLStatement stmt = stmtList.get(0);
                if (stmt instanceof SQLInsertStatement) {
                    SQLInsertStatement insertStmt = (SQLInsertStatement) stmt;

                    if (selectTableBuffer != null) {
                        selectTableBuffer.append(insertStmt.getTableName());
                    }

                    SQLSelect selectClause = insertStmt.getQuery();
                    if (selectClause != null) {
                        if (selectClauseBuffer != null) {
                            SQLASTOutputVisitor visitor = createFormatOutputVisitor(selectClauseBuffer, dbType);
                            selectClause.accept(visitor);
                        }

                        return true;
                    }
                }
            }

        }
        return false;
    }

    /**
     * 更改seqence的变量为null，为查询数量而提供
     */
    public static String changeSequenceItemToNull(String sql, String dbType) {
        StringBuilder finalSql = new StringBuilder();
        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
            List<SQLStatement> stmtList = parser.parseStatementList();
            if (stmtList == null || stmtList.size() < 1) {
                return sql;
            }

            SQLSelectStatement stmt = (SQLSelectStatement) stmtList.get(0);
            SQLSelect select = stmt.getSelect();
            handleFirstSelectColumns(dbType, select, select.getQuery(), finalSql);

        } catch (Exception e) {
            return sql;
        }

        return finalSql.toString();
    }

    public static String removeDatabaseEscapeChar(String tableName) {
        if (tableName.startsWith("\"")) {
            // oracle sqlserver 转义字符
            tableName = tableName.substring(1, tableName.length() - 1);
        }
        if (tableName.startsWith("`")) {
            // mysql 转义字符
            tableName = tableName.substring(1, tableName.length() - 1);
        }
        if (tableName.startsWith("[")) {
            // sql server 转义字符
            tableName = tableName.substring(1, tableName.length() - 1);
        }

        return tableName;
    }

    /**
     * 重命名column, 如果没有别名且没有冲突的字段名字, 返回null<br>
     * 如果有别名没有冲突,则返回别名, 否则返回 新的别名<br>
     * 把所有非特殊字符替换成_,并且在前面加autoRename_i_ i为第几个
     */
    public static String renameColumn(Set<String> columnNames, String columnName, String columnAlias) {
        String retVal = "";
        if (columnAlias != null) {
            retVal = columnAlias.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]+", "_").toUpperCase();

            while (retVal.startsWith("_")) {
                retVal = retVal.substring(1);
            }

            while (retVal.getBytes().length > NAME_LENGTH) {
                retVal = retVal.substring(0, retVal.length() - 1);
            }

            if (!columnNames.contains(retVal)) {
                columnNames.add(retVal);
                return retVal;
            }

            String tmpStr = retVal;
            for (int i = 1;; i++) {
                retVal = ("AR" + i) + (tmpStr.startsWith("_") ? "" : "_") + tmpStr;
                while (retVal.getBytes().length > NAME_LENGTH) {
                    retVal = retVal.substring(0, retVal.length() - 1);
                }

                if (!columnNames.contains(retVal)) {
                    columnNames.add(retVal);
                    return retVal;
                }
            }

        }
        return renameColumn(columnNames, columnName, columnName);
    }

    public static SQLASTOutputVisitor createFormatOutputVisitor(Appendable out, String dbType) {
        if (JdbcUtils.ORACLE.equals(dbType) || JdbcUtils.ALI_ORACLE.equals(dbType)) {
            return new OracleOutputVisitor(out, false);
        }

        if (JdbcUtils.MYSQL.equals(dbType)) {
            return new MySqlOutputVisitor(out);
        }

        if (JdbcUtils.SQL_SERVER.equals(dbType) || JdbcUtils.JTDS.equals(dbType)) {
            return new SQLServerOutputVisitor(out);
        }

        return new SQLASTOutputVisitor(out);
    }

    private static List<String> getSelectStatements(String sql, String type) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, type);
        List<SQLStatement> statementList = parser.parseStatementList();
        return statementList.stream()
                .filter(stmt -> stmt.getClass().getSimpleName().toLowerCase().contains(SELECT))
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    private static List<String> getQueryStatements(String sql, String type) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, type);
        List<SQLStatement> statementList = parser.parseStatementList();
        return statementList.stream()
                .filter(stmt -> StringUtils.containsAny(stmt.getClass().getSimpleName().toLowerCase(),
                        SELECT, SHOW_TABLES, SHOW_CREATE_TABLE, DESC))
                .map(stmt -> SqlUtils.sqlStatementToString(stmt, type))
                .collect(Collectors.toList());
    }

    private static String sqlStatementToString(SQLStatement stmt, String type) {
        if (stmt.getClass().getSimpleName().toLowerCase().contains(SELECT)) {
            return stmt.toString();
        } else {
            return SQLUtils.toSQLString(stmt, type);
        }
    }

    private static List<Pair<String, String>> getDataChangeStatements(String sql, String type) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, type);
        List<SQLStatement> statementList = parser.parseStatementList();
        List<Pair<String, String>> statements = statementList.stream()
                .filter(stmt -> StringUtils.containsAny(stmt.getClass().getSimpleName().toLowerCase(),
                        INSERT, UPDATE, DELETE))
                .map(stmt -> {
                    String stmtName = stmt.getClass().getSimpleName().toLowerCase();
                    String newSql = stmt.toString();
                    Pair<String, String> result;
                    if (stmtName.contains(INSERT)) {
                        result = Pair.of(INSERT, newSql);
                    } else if (stmtName.contains(UPDATE)) {
                        result = Pair.of(UPDATE, newSql);
                    } else {
                        result = Pair.of(DELETE, newSql);
                    }
                    return result;
                })
                .collect(Collectors.toList());
        if (statements.size() < statementList.size()) {
            throw new ParserException("only support {INSERT, UPDATE, DELETE} statement");
        }
        return statements;
    }

    private static List<String> getStructChangeStatements(byte type, String sql, String dbType) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        List<SQLStatement> statementList = parser.parseStatementList();

        List<String> statements = statementList.stream()
                .filter(stmt -> StructChange.ChangeType.CREATE == type
                        ? stmt.getClass().getSimpleName().toLowerCase().contains(CREATE)
                        : StringUtils.containsAny(stmt.getClass().getSimpleName().toLowerCase(), CREATE, ALTER, DROP, RENAME, TRUNCATE))
                .map(SQLStatement::toString)
                .collect(Collectors.toList());

        if (statements.size() < statementList.size()) {
            throw new ParserException(StructChange.ChangeType.CREATE == type
                    ? "CREATE DDL only support CREATE statement"
                    : "only support {CREATE, ALTER, DROP, RENAME, TRUNCATE} statement");
        }

        return statements;
    }

    /** get alter statement table names **/
    public static List<String> getAlterTableNames(String sql, String dbType) {
        return SQLParserUtils.createSQLStatementParser(sql, dbType)
                .parseStatementList().stream()
                .map(tableNameBy())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static Function<SQLStatement, String> tableNameBy() {
        return (stmt) -> stmt instanceof SQLAlterTableStatement
                ? removeDatabaseAndEscapeChar(((SQLAlterTableStatement) stmt).getTableSource().toString())
                : null;
    }

    public static String removeDatabaseAndEscapeChar(String tableName) {
        // only mysql 转义字符
        int index = tableName.indexOf('.');
        // database
        if (index != -1) {
            tableName = tableName.substring(index + 1);
        }
        // escape
        if (tableName.startsWith("`")) {

            tableName = tableName.substring(1, tableName.length() - 1);
        }
        return tableName;
    }

    public static void handleFirstSelectColumns(String dbType, SQLSelect select, SQLSelectQuery selectQuery,
                                                 StringBuilder finalSql) {
        if (selectQuery instanceof SQLUnionQuery) {
            // 是union , 按照统一规则重命名两侧
            SQLUnionQuery unionQuery = (SQLUnionQuery) selectQuery;
            handleFirstSelectColumns(dbType, select, unionQuery.getLeft(), finalSql);
            finalSql.append(" ").append(unionQuery.getOperator()).append(" ");
            handleFirstSelectColumns(dbType, select, unionQuery.getRight(), finalSql);
        } else {
            finalSql.append(" SELECT ");

            if (selectQuery instanceof SQLSelectQueryBlock) {
                switch (((SQLSelectQueryBlock) selectQuery).getDistionOption()) {
                    case SQLSetQuantifier.ALL:
                        finalSql.append(" ALL ");
                        break;
                    case SQLSetQuantifier.DISTINCT:
                        finalSql.append(" DISTINCT ");
                        break;
                    case SQLSetQuantifier.DISTINCTROW:
                        finalSql.append(" DISTINCTROW ");
                        break;
                }
            }

            appendSpecial(dbType, select, finalSql);
            SQLSelectQueryBlock selectQueryBlock = (SQLSelectQueryBlock) selectQuery;
            List<SQLSelectItem> selectItems = selectQueryBlock.getSelectList();
            Set<String> nameSet = new HashSet<>();
            for (SQLSelectItem si : selectItems) {
                String columnAlias = si.getAlias();
                SQLExpr expr = si.getExpr();
                String columnName = null;
                String columnPre = null;

                if (expr instanceof SQLPropertyExpr) {
                    SQLPropertyExpr proExpr = (SQLPropertyExpr) expr;
                    columnName = proExpr.getName();
                    columnPre = proExpr.getOwner().toString();
                    if ("nextval".equalsIgnoreCase(columnName) || "currval".equalsIgnoreCase(columnName)) {
                        // 是序列
                        columnName = "null";
                        columnPre = null;
                    }

                } else {
                    StringBuilder sb = new StringBuilder();
                    SQLASTVisitor visitor = createFormatOutputVisitor(sb, dbType);
                    expr.accept(visitor);
                    columnName = sb.toString();
                }

                String newAlias = renameColumn(nameSet, columnName, columnAlias);
                newAlias = filterDbReservedWords(newAlias, dbType);
                if (columnPre != null) {
                    finalSql.append(columnPre.toLowerCase()).append(".");
                }
                finalSql.append(columnName).append(" ");
                if (newAlias != null) {
                    finalSql.append(" AS ").append(newAlias.toLowerCase());
                } else if (columnAlias != null) {
                    finalSql.append(" AS ").append(columnAlias.toLowerCase());
                }
                finalSql.append(", ");
            }

            finalSql.delete(finalSql.lastIndexOf(", "), finalSql.length());

            // 添加from
            StringBuilder fromStr = new StringBuilder();
            SQLASTOutputVisitor visitor = createFormatOutputVisitor(fromStr, dbType);
            selectQueryBlock.getFrom().accept(visitor);
            finalSql.append(" FROM ").append(fromStr.toString());

            // where 部分
            SQLExpr whereSource = selectQueryBlock.getWhere();
            StringBuilder whereStr = new StringBuilder();
            SQLASTOutputVisitor whereisitor = createFormatOutputVisitor(whereStr, dbType);
            if (whereSource != null) {
                whereSource.accept(whereisitor);
                finalSql.append(" WHERE ").append(whereStr.toString());
            }

            // group子句
            StringBuilder group = new StringBuilder();
            SQLASTOutputVisitor groupVisitor = createFormatOutputVisitor(group, dbType);
            SQLSelectGroupByClause groupClause = selectQueryBlock.getGroupBy();
            if (groupClause != null) {
                groupClause.accept(groupVisitor);
                finalSql.append(" ").append(group.toString());
            }

            // order子句
            StringBuilder order = new StringBuilder();
            SQLASTOutputVisitor orderVisitor = createFormatOutputVisitor(order, dbType);

            SQLOrderBy orderClause;
            if (JdbcUtils.MYSQL.equalsIgnoreCase(dbType)) {
                MySqlSelectQueryBlock mysqlSelectQuery = (MySqlSelectQueryBlock) select.getQuery();
                orderClause = mysqlSelectQuery.getOrderBy();
            } else {
                orderClause = select.getOrderBy();
            }
            if (orderClause != null) {
                orderClause.accept(orderVisitor);
                finalSql.append(" ").append(order.toString());
            }

            // 特殊尾部
            generateSQLLeftIndividualPart(dbType, select, finalSql, selectQueryBlock);
        }
    }

    /**
     * 转换sql,得到select部分
     */
    public static SQLSelect getSQLSelect(String sql, String dbType) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        List<SQLStatement> stmtList = parser.parseStatementList();
        SQLSelectStatement stmt = (SQLSelectStatement) stmtList.get(0);
        return stmt.getSelect();
    }

    private static void appendSpecial(String dbType, SQLSelect select, StringBuilder finalSql) {
        if (JdbcUtils.SQL_SERVER.equalsIgnoreCase(dbType)) {
            SQLSelectQuery query = select.getQuery();
            if (query instanceof SQLServerSelectQueryBlock) {
                SQLServerSelectQueryBlock sqlserverQuery = (SQLServerSelectQueryBlock) query;
                SQLServerTop top = sqlserverQuery.getTop();
                if (top != null) {
                    SQLIntegerExpr expr = (SQLIntegerExpr) top.getExpr();
                    finalSql.append(" top ").append(expr.getNumber().intValue()).append(" ");
                }
            }
        }
    }

    private static void generateSQLLeftIndividualPart(String dbType, SQLSelect select, StringBuilder finalSql,
                                                      SQLSelectQueryBlock selectQueryBlock) {
        if (JdbcUtils.MYSQL.equalsIgnoreCase(dbType)) {
            // limit子句
            StringBuilder limit = new StringBuilder();
            SQLASTOutputVisitor limitVisitor = createFormatOutputVisitor(limit, dbType);
            MySqlSelectQueryBlock.Limit limitClause = ((MySqlSelectQueryBlock) selectQueryBlock).getLimit();
            if (limitClause != null) {
                limitClause.accept(limitVisitor);
                finalSql.append(" ").append(limit.toString());
            }
        }

    }

    /**
     * 过滤数据库本身的关键字，转成`keyword`的形式
     */
    public static String filterDbReservedWords(String columnName, String dbType) {
        final String[] mysqlReservedWords = { "ACCESSIBLE", "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC",
                "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE",
                "CHANGE", "CHAR", "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION", "CONSTRAINT", "CONTINUE",
                "CONVERT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER",
                "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC",
                "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT",
                "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED",
                "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE", "FOREIGN",
                "FROM", "FULLTEXT", "GET", "GRANT", "GROUP", "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND",
                "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE",
                "INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERVAL", "INTO",
                "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "IS", "ITERATE", "JOIN", "KEY", "KEYS", "KILL", "LEADING",
                "LEAVE", "LEFT", "LIKE", "LIMIT", "LINEAR", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCK",
                "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MASTER_BIND", "MASTER_SSL_VERIFY_SERVER_CERT",
                "MATCH", "MAXVALUE", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND",
                "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON",
                "OPTIMIZE", "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "PARTITION", "PRECISION",
                "PRIMARY", "PROCEDURE", "PURGE", "RANGE", "READ", "READS", "READ_WRITE", "REAL", "REFERENCES",
                "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESIGNAL", "RESTRICT", "RETURN",
                "REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE",
                "SEPARATOR", "SET", "SHOW", "SIGNAL", "SMALLINT", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION",
                "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SSL",
                "STARTING", "STRAIGHT_JOIN", "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO",
                "TRAILING", "TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE",
                "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR",
                "VARCHARACTER", "VARYING", "WHEN", "WHERE", "WHILE", "WITH", "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL",
                "GET", "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "MASTER_BIND" };
        final String[] oracleReservedWords = { "ACCESS", "ELSE", "MODIFY", "START", "ADD", "EXCLUSIVE", "NOAUDIT",
                "SELECT", "ALL", "EXISTS", "NOCOMPRESS", "SESSION", "ALTER", "FILE", "NOT", "SET", "AND", "FLOAT",
                "NOTFOUND", "SHARE", "ANY", "FOR", "NOWAIT", "SIZE", "ARRAYLEN", "FROM", "NULL", "SMALLINT", "AS",
                "GRANT", "NUMBER", "SQLBUF", "ASC", "GROUP", "OF", "SUCCESSFUL", "AUDIT", "HAVING", "OFFLINE",
                "SYNONYM", "BETWEEN", "IDENTIFIED", "ON", "SYSDATE", "BY", "IMMEDIATE", "ONLINE", "TABLE", "CHAR",
                "IN", "OPTION", "THEN", "CHECK", "INCREMENT", "OR", "TO", "CLUSTER", "INDEX", "ORDER", "TRIGGER",
                "COLUMN", "INITIAL", "PCTFREE", "UID", "COMMENT", "INSERT", "PRIOR", "UNION", "COMPRESS", "INTEGER",
                "PRIVILEGES", "UNIQUE", "CONNECT", "INTERSECT", "PUBLIC", "UPDATE", "CREATE", "INTO", "RAW", "USER",
                "CURRENT", "IS", "RENAME", "VALIDATE", "DATE", "LEVEL", "RESOURCE", "VALUES", "DECIMAL", "LIKE",
                "REVOKE", "VARCHAR", "DEFAULT", "LOCK", "ROW", "VARCHAR2", "DELETE", "LONG", "ROWID", "VIEW", "DESC",
                "MAXEXTENTS", "ROWLABEL", "WHENEVER", "DISTINCT", "MINUS", "ROWNUM", "WHERE", "DROP", "MODE", "ROWS",
                "WITH" };

        if (JdbcUtils.MYSQL.equalsIgnoreCase(dbType)) {
            for (String kw : mysqlReservedWords) {
                if (kw.equalsIgnoreCase(columnName)) {
                    columnName = "`" + columnName + "`";
                    break;
                }
            }
        } else if (JdbcUtils.ORACLE.equalsIgnoreCase(dbType)) {
            for (String kw : oracleReservedWords) {
                if (kw.equalsIgnoreCase(columnName)) {
                    columnName = "\"" + columnName + "\"";
                    break;
                }
            }
        }

        return columnName;
    }
}
