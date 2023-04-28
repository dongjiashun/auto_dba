package com.autodb.ops.dms.domain.datasource.sql;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOver;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateOption;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectHierachicalQueryClause;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectQueryBlock;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.util.JdbcUtils;
import com.google.common.base.Splitter;
import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.common.util.SqlUtils;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.domain.datasource.visitor.TableNameInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract SQLService<br/>
 * copy from dms old code ...
 *
 * @author dongjs
 * @since 16/2/17
 */
public abstract class AbstractSQLService implements SQLService {
    private static final String MASK_STRING = "******";
    private static final String HIDDEN_PREFIX = Constants.HIDDEN_PREFIX;

    // 非嵌套，非join，union
    private static final int FROM_TYPE_NORMAL = 0;
    // 嵌套自查旬
    private static final int FROM_TYPE_NEST = 1;
    private static final int FROM_TYPE_JOIN = 2;

    // 是普通函数
    private static final int EXPR_TYPE_METHOD = 11;
    // 是count函数,*不用解析
    private static final int EXPR_TYPE_METHOD_COUNT = 12;
    // 正常的处理,把*转换成列名
    private static final int EXPR_TYPE_NORMAL = 13;
    // 正常的处理,把*转换成列名
    private static final int EXPR_TYPE_CASE_WHEN = 14;
    // 正常的处理,把*转换成列名
    private static final int EXPR_TYPE_CASE_RESULT = 15;

    // join的表(可能是子查询),
    private final Map<String, String> joinTables = new HashMap<String, String>();

    private final Logger logger = LoggerFactory.getLogger(AbstractSQLService.class);

    @Override
    public String securityMaskSql(String sql, Map<String, Set<String>> securityData,
                                  DatabaseVisitor visitor, Set<String> tableSet) {
        Objects.requireNonNull(sql);
        Objects.requireNonNull(securityData);
        Objects.requireNonNull(visitor);

        if (securityData.size() == 0) {
            return sql;
        }

        try {
            StringBuilder maskSql = new StringBuilder();
            // 对select部分进行字段脱敏
            recursiveMaskSql(securityData, visitor, SqlUtils.getSQLSelect(sql, this.getDbType()), maskSql, tableSet);

            // 对最外层查询的字段进行重名处理
            return autoRename(maskSql.toString());
        } catch (Exception e) {
            logger.error("mask sql [" + sql + "] exception, {}", e.getMessage());
            return sql;
        }
    }

    private void recursiveMaskSql(Map<String, Set<String>> securityData, DatabaseVisitor visitor,
                                  SQLSelect select, StringBuilder finalSql, Set<String> tableSet)
            throws Exception {
        SQLSelectQuery query = select.getQuery();

        if (query instanceof SQLUnionQuery) {
            handleUnionQuery(securityData, visitor, select, (SQLUnionQuery) query, finalSql, tableSet);
        } else {
            // 用来装表的别名,用set是为了防止重复,为上面的joinTables服务,获取唯一的新的表别名
            Set<String> tableAliasSet = new HashSet<>();
            handleSelectQuery(securityData, visitor, tableAliasSet, select, (SQLSelectQueryBlock) query,
                    finalSql, tableSet);
        }
    }

    /**
     * 处理union 情况
     */
    private void handleUnionQuery(Map<String, Set<String>> securityData, DatabaseVisitor visitor, SQLSelect select,
                                  SQLUnionQuery query, StringBuilder finalSql, Set<String> tableSet) throws Exception {
        // 处理左侧
        SQLSelectQuery leftQuery = query.getLeft();
        if (leftQuery instanceof SQLUnionQuery) {
            handleUnionQuery(securityData, visitor, select, (SQLUnionQuery) leftQuery, finalSql, tableSet);
        } else {
            Set<String> tableAliasSet = new HashSet<>();
            handleSelectQuery(securityData, visitor, tableAliasSet, select, (SQLSelectQueryBlock) leftQuery,
                    finalSql, tableSet);
        }

        // 处理连接
        finalSql.append(" ").append(query.getOperator()).append(" ");

        // 处理右侧
        SQLSelectQuery rightQuery = query.getRight();
        if (rightQuery instanceof SQLUnionQuery) {
            handleUnionQuery(securityData, visitor, select, (SQLUnionQuery) rightQuery, finalSql, tableSet);
        } else {
            Set<String> tableAliasSet = new HashSet<>();
            handleSelectQuery(securityData, visitor, tableAliasSet, select, (SQLSelectQueryBlock) rightQuery,
                    finalSql, tableSet);
        }
    }

    /**
     * 处理 普通select（无union）
     */
    private void handleSelectQuery(Map<String, Set<String>> securityData, DatabaseVisitor visitor,
                                   Set<String> tableAliasSet, SQLSelect select, SQLSelectQueryBlock selectQuery,
                                   StringBuilder finalSql, Set<String> tableSet) throws Exception {
        SQLTableSource fromClause = selectQuery.getFrom();
        int fromType = FROM_TYPE_NORMAL;
        if (fromClause instanceof SQLSubqueryTableSource) {
            fromType = FROM_TYPE_NEST;
        } else if (fromClause instanceof SQLJoinTableSource) {
            fromType = FROM_TYPE_JOIN;
        }

        // select 部分
        finalSql.append("select ");
        this.appendSpecial(visitor, select, finalSql);

        // 遍历查询字段
        switch (selectQuery.getDistionOption()) {
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

        List<SQLSelectItem> selectItems = selectQuery.getSelectList();
        for (SQLSelectItem si : selectItems) {
            String columnAlias = si.getAlias();
            SQLExpr expr = si.getExpr();

            handleExpr(securityData, visitor, tableAliasSet, expr, columnAlias, fromClause, finalSql, fromType,
                    EXPR_TYPE_NORMAL, tableSet);
            finalSql.append(", ");

        }
        // 删除最后的,select结束， 添加 from 部分
        finalSql.delete(finalSql.lastIndexOf(", "), finalSql.length()).append(" from ");

        // from部分
        if (fromType == FROM_TYPE_NEST) {
            finalSql.append(" ( ");
            SQLSubqueryTableSource subSelect = (SQLSubqueryTableSource) fromClause;
            recursiveMaskSql(securityData, visitor, subSelect.getSelect(), finalSql, tableSet);
            finalSql.append(" ) ");
            String fromAlias = fromClause.getAlias();
            if (fromAlias != null) {
                finalSql.append(fromAlias.toLowerCase());
            } else {
                StringBuilder tableStr = new StringBuilder();
                SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(tableStr, this.getDbType());
                fromClause.accept(outputVisitor);
                fromAlias = joinTables.get(tableStr.toString().toUpperCase().replaceAll("\\s", ""));
                if (fromAlias != null) {
                    finalSql.append(fromAlias.toLowerCase());
                }
            }
        } else if (fromType == FROM_TYPE_JOIN) {
            SQLJoinTableSource joinCluse = (SQLJoinTableSource) fromClause;
            handleFromJoinSource(securityData, joinCluse, finalSql, visitor, tableSet);
        } else {
            // 直接接表名
            SQLExprTableSource sqlExpr = (SQLExprTableSource) fromClause;
            String tableName = sqlExpr.getExpr().toString();
            String alias = sqlExpr.getAlias();

            // 如果不是view,直接接表名,否则转换为对应的view的sql语句
            if (visitor.isView(tableName)) {
                String viewSql = visitor.getViewCreateSql(tableName);

                finalSql.append(" (");
                recursiveMaskSql(securityData, visitor, SqlUtils.getSQLSelect(viewSql, this.getDbType()), finalSql, tableSet);
                finalSql.append(" )");

                if (null != alias) {
                    finalSql.append(" ").append(alias.toLowerCase()).append(" ");
                } else {
                    finalSql.append(tableName.toLowerCase());
                }
            } else {
                finalSql.append(" ").append(tableName.toLowerCase()).append(" ");
                if (null != alias) {
                    finalSql.append(" ").append(alias.toLowerCase()).append(" ");
                }
            }

            // 记录表名
            tableSet.add(tableName.toUpperCase());
        }

        // where 部分
        SQLExpr whereSource = selectQuery.getWhere();
        if (whereSource != null) {
            StringBuffer sb = new StringBuffer();
            SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(sb, this.getDbType());
            whereSource.accept(outputVisitor);
            finalSql.append(" WHERE ").append(sb.toString());
        }

        // connect by 部分, 只有oracle 有
        if (selectQuery instanceof OracleSelectQueryBlock) {
            OracleSelectQueryBlock oracleSelect = (OracleSelectQueryBlock) selectQuery;
            OracleSelectHierachicalQueryClause oracleSHQClause = oracleSelect.getHierachicalQueryClause();
            if (null != oracleSHQClause) {
                StringBuffer sb = new StringBuffer();
                SQLASTOutputVisitor oracleSHQVisitor = SqlUtils.createFormatOutputVisitor(sb, this.getDbType());
                oracleSHQClause.accept(oracleSHQVisitor);
                finalSql.append(" ").append(sb.toString()).append(" ");
            }
        }

        // group子句
        SQLSelectGroupByClause groupClause = selectQuery.getGroupBy();
        if (groupClause != null) {
            StringBuffer group = new StringBuffer();
            SQLASTOutputVisitor groupVisitor = SqlUtils.createFormatOutputVisitor(group, this.getDbType());
            groupClause.accept(groupVisitor);
            finalSql.append(" ").append(group.toString());
        }

        // order子句
        SQLOrderBy orderClause = null;
        if (JdbcUtils.MYSQL.equalsIgnoreCase(this.getDbType())) {
            MySqlSelectQueryBlock mysqlSelectQuery = (MySqlSelectQueryBlock) select.getQuery();
            orderClause = mysqlSelectQuery.getOrderBy();
        } else {
            orderClause = select.getOrderBy();
        }
        if (orderClause != null) {
            StringBuffer order = new StringBuffer();
            SQLASTOutputVisitor orderVisitor = SqlUtils.createFormatOutputVisitor(order, this.getDbType());
            orderClause.accept(orderVisitor);
            finalSql.append(" ").append(order.toString());
        }

        // 特殊尾部
        generateSQLLeftIndividualPart(visitor, select, finalSql, selectQuery);
    }

    /**
     * 处理字段
     */
    private void handleExpr(Map<String, Set<String>> securityDataMap, DatabaseVisitor visitor, Set<String> tableAliasSet,
                            SQLExpr expr, String columnAlias, SQLTableSource fromClause, StringBuilder finalSql,
                            final int fromType, final int exprType, Set<String> tableSet) throws Exception {

        if (columnAlias != null) {
            // 去除别名中的转义字符
            columnAlias = SqlUtils.removeDatabaseEscapeChar(columnAlias);
        }


        if (expr instanceof SQLCharExpr) {
            // 是字符常量
            SQLCharExpr charxpr = (SQLCharExpr) expr;
            finalSql.append(" '").append(charxpr.getText()).append('\'');
            if (columnAlias != null) {
                finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
            }
        } else if (expr instanceof SQLNullExpr) {
            // 是null常量
            finalSql.append(" ").append("null");
            if (columnAlias != null) {
                finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
            }
        } else if (expr instanceof SQLNumericLiteralExpr) {
            // 是数字常量
            SQLNumericLiteralExpr numbericExp = (SQLNumericLiteralExpr) expr;
            String columnName = numbericExp.toString();
            finalSql.append(" ").append(columnName.toLowerCase());
            if (columnAlias != null) {
                finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
            }
        } else if (expr instanceof SQLAllColumnExpr) {
            // 是一个*号,
            if (exprType == EXPR_TYPE_METHOD_COUNT) {
                finalSql.append(" * ");
            } else {
                if (fromClause instanceof SQLJoinTableSource) {
                    SQLJoinTableSource sqlJoin = (SQLJoinTableSource) fromClause;
                    handleJoinSelectAll(securityDataMap, sqlJoin, tableAliasSet, finalSql, visitor, tableSet);
                } else {
                    // （不是join类型, from后面有一个表(可能是子查询),）替换成具体的字段
                    handleCommonSelectAll(securityDataMap, tableAliasSet, fromClause, finalSql, visitor, tableSet);
                }
            }
        } else {
            // 只有一个单一字段名，a
            if (expr instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr idExpr = (SQLIdentifierExpr) expr;
                String columnName = idExpr.getName();

                columnName = SqlUtils.removeDatabaseEscapeChar(columnName);

                if (fromType == FROM_TYPE_JOIN) {
                    SQLJoinTableSource joinCluse = (SQLJoinTableSource) fromClause;

                    // 每个字段只能属于一个表
                    // 获取join的表名(有可能是临时表), 遍历查询其字段, 以第一个查到得优先判定这个字段属于该表(不可能属于多个表,若属于多个则是语法错误)

                    List<TableNameInfo> joinTables = new ArrayList<>();
                    handleJoinTables(joinCluse, joinTables, this.getDbType(), tableSet);
                    TableNameInfo tFlag = null;
                    for (TableNameInfo tn : joinTables) {
                        if (tFlag != null) {
                            break;
                        }

                        // 查询该表(子查询)会产生的字段
                        String queryFieldSql = "select * from " + tn.getName() + " where 0=1";
                        List<String> columns = visitor.getQueryColumnLabels(queryFieldSql);
                        for (String tmpCol : columns) {
                            if (tmpCol.equalsIgnoreCase(columnName)) {
                                // 找到该字段所属于的表
                                tFlag = tn;
                                break;
                            }
                        }
                    }

                    if (tFlag.isNestQuery()) {
                        // 是嵌套查询,直接拼装该字段
                        if (tFlag.getAlias() != null) {
                            finalSql.append(tFlag.getAlias().toLowerCase()).append(".");
                        }
                        finalSql.append(columnName.toLowerCase()).append(" ");
                    } else {
                        // 不是子查询, 并进行脱敏
                        maskColumn(securityDataMap, columnName, columnAlias, tFlag.getName(), tFlag.getAlias(),
                                finalSql, fromType, exprType);

                    }

                } else if (fromType == FROM_TYPE_NEST) {
                    // 是子查询,直接拼装,不做处理
                    finalSql.append(" ").append(columnName.toLowerCase()).append(" ");
                    if (columnAlias != null) {
                        finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
                    }
                } else {
                    StringBuffer tableStr = new StringBuffer();
                    SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(tableStr, this.getDbType());
                    fromClause.accept(outputVisitor);
                    String tableName = null;
                    if (fromClause instanceof SQLExprTableSource) {
                        // 是一个单纯的表
                        SQLExprTableSource exprTable = (SQLExprTableSource) fromClause;
                        SQLExpr tableExpr = exprTable.getExpr();
                        if (tableExpr instanceof SQLPropertyExpr) {
                            tableName = ((SQLPropertyExpr) tableExpr).getName();
                            tableName = SqlUtils.removeDatabaseEscapeChar(tableName);
                        } else if (tableExpr instanceof SQLIdentifierExpr) {
                            tableName = SqlUtils.removeDatabaseEscapeChar(tableExpr.toString());
                        }
                    } else {
                        if (tableStr.toString().startsWith("(")) {
                            tableName = tableStr.toString();
                        } else {
                            tableName = "(" + tableStr + ") tmpTable";
                        }
                    }

                    String tmpTableAlias = fromClause.getAlias();

                    // count(distinct xxx)
                    SQLAggregateExpr parentAggExpr = null;
                    if (idExpr.getParent() != null && idExpr.getParent() instanceof SQLAggregateExpr) {
                        parentAggExpr = (SQLAggregateExpr) idExpr.getParent();
                    }
                    maskColumn(securityDataMap, columnName, columnAlias, tableName, tmpTableAlias, finalSql, fromType,
                            exprType, parentAggExpr);
                }
            } else if (expr instanceof SQLPropertyExpr) {
                // 有表前缀，t.a
                // 根据前缀 和 表名(别名) 进行判断属于哪一个表
                SQLPropertyExpr spExpr = (SQLPropertyExpr) expr;
                String columnName = SqlUtils.removeDatabaseEscapeChar(spExpr.getName());
                String tableAlias = spExpr.getOwner().toString();
                // String tableName = getTableNameByColumnName(tableMap.get(1), tableAlias);

                StringBuffer tableStr = new StringBuffer();
                SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(tableStr, this.getDbType());
                fromClause.accept(outputVisitor);
                String tableName = null;

                if (fromClause instanceof SQLExprTableSource) {
                    // 是一个单纯的表
                    SQLExprTableSource exprTable = (SQLExprTableSource) fromClause;
                    SQLExpr tableExpr = exprTable.getExpr();
                    if (tableExpr instanceof SQLPropertyExpr) {
                        tableName = ((SQLPropertyExpr) tableExpr).getName();
                        tableName = SqlUtils.removeDatabaseEscapeChar(tableName);
                    } else if (tableExpr instanceof SQLIdentifierExpr) {
                        tableName = SqlUtils.removeDatabaseEscapeChar(tableExpr.toString());
                    }
                } else {
                    if (tableStr.toString().startsWith("(")) {
                        tableName = tableStr.toString();
                    } else {
                        tableName = "(" + tableStr + ") tmpTable";
                    }
                }

                if (fromType == FROM_TYPE_JOIN) {
                    SQLJoinTableSource joinCluse = (SQLJoinTableSource) fromClause;
                    List<TableNameInfo> joinTables = new ArrayList<TableNameInfo>();
                    handleJoinTables(joinCluse, joinTables, this.getDbType(), tableSet);
                    TableNameInfo tFlag = null;
                    // 根据字段前缀找到所属于的表, 先根据表的别名判定,如果不符合,再根据表的全名进行查找
                    for (TableNameInfo tn : joinTables) {
                        if (tableAlias.equalsIgnoreCase(tn.getAlias()) || tableAlias.equalsIgnoreCase(tn.getName())) {
                            tFlag = tn;
                            break;
                        }
                    }

                    if (tFlag.isNestQuery()) {
                        // 是嵌套查询
                        if ("*".equals(columnName)) {
                            // 查询所有,
                            // 查询该表(子查询)会产生的字段, 直接拼装
                            String queryFieldSql = "select * from " + tFlag.getName() + " where 0=1";
                            List<String> columns = visitor.getQueryColumnLabels(queryFieldSql);
                            for (String tmpCol : columns) {
                                finalSql.append(tableAlias.toLowerCase()).append(".").append(tmpCol.toLowerCase()).append(", ");
                            }
                            finalSql.delete(finalSql.lastIndexOf(", "), finalSql.length()).append(" ");
                        } else {
                            finalSql.append(tableAlias.toLowerCase()).append(".").append(columnName.toLowerCase()).append(" ");
                            if (exprType == EXPR_TYPE_NORMAL && columnAlias != null) {
                                finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
                            }
                        }
                    } else {
                        // 非嵌套查询, 进行字段脱敏
                        if (exprType == EXPR_TYPE_METHOD_COUNT) {
                            // 是count, 不对其脱敏
                            finalSql.append(" ").append(tableAlias.toLowerCase()).append(".").append(columnName.toLowerCase()).append(" ");
                        } else {
                            if ("*".equals(columnName)) {
                                // 查询所有,
                                // 查询该表(子查询)会产生的字段, 直接拼装
                                String queryFieldSql = "select * from " + tFlag.getName() + " where 0=1";
                                List<String> columns = visitor.getQueryColumnLabels(queryFieldSql);
                                maskColumns(securityDataMap, tFlag.getName(), tableAlias, columns, finalSql, fromType);
                            } else {
                                maskColumn(securityDataMap, columnName, columnAlias, tFlag.getName(), tableAlias,
                                        finalSql, fromType, exprType);
                            }
                        }

                    }

                } else {
                    // 非 join 类型
                    if ("*".equals(columnName)) {
                        String queryFieldSql = "select * from " + tableName + " where 1=0 ";
                        List<String> columns = visitor.getQueryColumnLabels(queryFieldSql);
                        finalSql.append(" ");

                        // 如果from不是嵌套查询语句，那么此from表是最终表，取出该表名并获取敏感字段
                        maskColumns(securityDataMap, tableName, tableAlias, columns, finalSql, fromType);

                    } else {
                        maskColumn(securityDataMap, columnName, columnAlias, tableName, fromClause.getAlias(),
                                finalSql, fromType, exprType);
                    }
                }
            } else if (expr instanceof SQLBinaryOpExpr) {
                // 有运算符
                finalSql.append("(");
                handleSQLBinaryOpExpr(securityDataMap, tableAliasSet, (SQLBinaryOpExpr) expr, fromClause, finalSql,
                        visitor, fromType, EXPR_TYPE_METHOD, tableSet);
                finalSql.append(") ");
                if (columnAlias != null) {
                    finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
                }
            } else if (expr instanceof SQLAggregateExpr) {
                // 聚集函数
                SQLAggregateExpr mexpr = (SQLAggregateExpr) expr;
                sqlAggregateMethodWrapSecurityData(securityDataMap, visitor, tableAliasSet, mexpr, fromClause,
                        finalSql, tableSet);
                if (columnAlias != null) {
                    finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
                }
            } else if (expr instanceof SQLMethodInvokeExpr) {
                // 方法调用，比如Oracle里的字段合并方法WMSYS.WM_concat
                methodWrapSecurityData(securityDataMap, visitor, tableAliasSet, (SQLMethodInvokeExpr) expr,
                        fromClause, finalSql, tableSet);
                if (columnAlias != null) {
                    finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
                }
            } else if (expr instanceof SQLQueryExpr) {
                // 是一个子查询，进行递归
                SQLQueryExpr queryExpr = (SQLQueryExpr) expr;
                SQLSelect subSelect = queryExpr.getSubQuery();
                finalSql.append(" ( ");
                recursiveMaskSql(securityDataMap, visitor, subSelect, finalSql, tableSet);
                finalSql.append(" ) ");
                if (columnAlias != null) {
                    finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
                }
            } else if (expr instanceof SQLCaseExpr) {
                // 是一个case when
                finalSql.append(" case ");
                SQLCaseExpr caseExpr = (SQLCaseExpr) expr;
                SQLExpr valueExpr = caseExpr.getValueExpr();
                if (null != valueExpr) {
                    handleExpr(securityDataMap, visitor, tableAliasSet, valueExpr, null, fromClause, finalSql,
                            fromType, EXPR_TYPE_CASE_WHEN, tableSet);
                }
                List<SQLCaseExpr.Item> items = caseExpr.getItems();
                for (SQLCaseExpr.Item it : items) {
                    finalSql.append(" when ");
                    handleExpr(securityDataMap, visitor, tableAliasSet, it.getConditionExpr(), null, fromClause,
                            finalSql, fromType, EXPR_TYPE_CASE_WHEN, tableSet);
                    finalSql.append(" then ");
                    handleExpr(securityDataMap, visitor, tableAliasSet, it.getValueExpr(), null, fromClause,
                            finalSql, fromType, EXPR_TYPE_CASE_RESULT, tableSet);
                }

                SQLExpr elseExpr = caseExpr.getElseExpr();
                if (null != elseExpr) {
                    finalSql.append(" else ");
                    handleExpr(securityDataMap, visitor, tableAliasSet, elseExpr, null, fromClause, finalSql,
                            fromType, EXPR_TYPE_CASE_RESULT, tableSet);
                }

                finalSql.append(" end ");

                if (null != columnAlias) {
                    finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
                }

            } else {
                // 其他 比如 oracle sysdate
                StringBuilder sb = new StringBuilder();
                SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(sb, this.getDbType());
                expr.accept(outputVisitor);
                finalSql.append(" ").append(sb.toString());
            }
        }

    }

    /**
     * 处理from子句
     */
    private void handleFromJoinSource(Map<String, Set<String>> securityDataMap, SQLJoinTableSource joinCluse,
                                      StringBuilder finalSql, DatabaseVisitor visitor, Set<String> tableSet) throws Exception {
        // 左侧
        SQLTableSource leftJoin = joinCluse.getLeft();
        if (leftJoin instanceof SQLJoinTableSource) {
            // 是一个join
            handleFromJoinSource(securityDataMap, (SQLJoinTableSource) leftJoin, finalSql, visitor, tableSet);
        } else if (leftJoin instanceof SQLSubqueryTableSource) {
            // 是一个子查询
            SQLSubqueryTableSource subQuery = (SQLSubqueryTableSource) leftJoin;
            StringBuilder subStr = new StringBuilder();
            SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(subStr, this.getDbType());
            subQuery.accept(outputVisitor);
            if (!subStr.toString().startsWith("(")) {
                subStr.insert(0, "(");
                subStr.append("");
            }

            // 得到该子查询的select
            StringBuilder selectStr = new StringBuilder(subStr.toString());
            selectStr.delete(0, selectStr.indexOf("(") + 1);
            selectStr.delete(selectStr.lastIndexOf(")"), selectStr.length());
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(selectStr.toString(), this.getDbType());
            List<SQLStatement> stmtList = parser.parseStatementList();

            SQLSelectStatement stmt = (SQLSelectStatement) stmtList.get(0);
            finalSql.append(" (");
            recursiveMaskSql(securityDataMap, visitor, stmt.getSelect(), finalSql, tableSet);
            finalSql.append(" ) ");

            String alias = leftJoin.getAlias();
            if (alias != null) {
                finalSql.append(alias.toLowerCase()).append(" ");
            } else {
                alias = joinTables.get(subStr.toString().toUpperCase().replaceAll("\\s", ""));
                // select * from (select * from dms_user)
                // 防止这种嵌套子查询没有别名的状况
                finalSql.append(alias.toLowerCase()).append(" ");
            }
        } else {
            // 是表 直接追加
            SQLExprTableSource sqlExpr = (SQLExprTableSource) leftJoin;
            String tableName = sqlExpr.getExpr().toString();
            String alias = sqlExpr.getAlias();

            // 如果是view，则把创建view的语句拼到此处
            if (visitor.isView(tableName)) {
                String viewSql = visitor.getViewCreateSql(tableName);
                finalSql.append(" (");
                recursiveMaskSql(securityDataMap, visitor, SqlUtils.getSQLSelect(viewSql, getDbType()),
                        finalSql, tableSet);
                finalSql.append(" )");

                if (null != alias) {
                    finalSql.append(" ").append(alias.toLowerCase()).append(" ");
                } else {
                    finalSql.append(" ").append(tableName.toLowerCase()).append(" ");
                }
            } else {
                finalSql.append(" ").append(tableName.toLowerCase()).append(" ");
                if (null != alias) {
                    finalSql.append(" ").append(alias.toLowerCase()).append(" ");
                }
            }

        }

        // 添加条件
        SQLBinaryOpExpr joinOperator = (SQLBinaryOpExpr) joinCluse.getCondition();
        StringBuilder whereCondition = new StringBuilder();
        SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(whereCondition, this.getDbType());
        // where 连接没有joinOperator
        if (joinOperator != null) {
            joinOperator.accept(outputVisitor);
        }

        SQLJoinTableSource.JoinType joinType = joinCluse.getJoinType();
        finalSql.append(joinType.name);

        // 右侧
        SQLTableSource ringhtJoin = joinCluse.getRight();
        if (ringhtJoin instanceof SQLJoinTableSource) {
            // 是一个join
            handleFromJoinSource(securityDataMap, (SQLJoinTableSource) ringhtJoin, finalSql, visitor, tableSet);
        } else if (ringhtJoin instanceof SQLSubqueryTableSource) {
            // 是一个子查询
            SQLSubqueryTableSource subQuery = (SQLSubqueryTableSource) ringhtJoin;
            StringBuilder subStr = new StringBuilder();
            outputVisitor = SqlUtils.createFormatOutputVisitor(subStr, this.getDbType());
            subQuery.accept(outputVisitor);
            if (!subStr.toString().startsWith("(")) {
                subStr.insert(0, "(");
                subStr.append("");
            }

            // 得到该子查询的select
            StringBuilder selectStr = new StringBuilder(subStr.toString());
            selectStr.delete(0, selectStr.indexOf("(") + 1);
            selectStr.delete(selectStr.lastIndexOf(")"), selectStr.length());
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(selectStr.toString(), this.getDbType());
            List<SQLStatement> stmtList = parser.parseStatementList();

            SQLSelectStatement stmt = (SQLSelectStatement) stmtList.get(0);
            finalSql.append(" (");
            recursiveMaskSql(securityDataMap, visitor, stmt.getSelect(), finalSql, tableSet);
            finalSql.append(" ) ");

            String alias = ringhtJoin.getAlias();
            if (alias != null) {
                finalSql.append(alias.toLowerCase()).append(" ");
            } else {
                alias = joinTables.get(subStr.toString().toUpperCase().replaceAll("\\s", ""));
                // select * from (select * from dms_user)
                // 防止这种嵌套子查询没有别名的状况
                finalSql.append(alias.toLowerCase()).append(" ");
            }
        } else {
            // 是表 直接追加
            SQLExprTableSource sqlExpr = (SQLExprTableSource) ringhtJoin;
            String alias = sqlExpr.getAlias();
            String tableName = sqlExpr.getExpr().toString();

            // 如果是view，则把创建view的语句拼到此处
            if (visitor.isView(tableName)) {
                String viewSql = visitor.getViewCreateSql(tableName);
                finalSql.append(" (");
                recursiveMaskSql(securityDataMap, visitor, SqlUtils.getSQLSelect(viewSql, this.getDbType()),
                        finalSql, tableSet);
                finalSql.append(" )");

                if (null != alias) {
                    finalSql.append(" ").append(alias.toLowerCase()).append(" ");
                } else {
                    finalSql.append(" ").append(tableName.toLowerCase()).append(" ");
                }
            } else {
                finalSql.append(" ").append(tableName.toLowerCase()).append(" ");
                if (null != alias) {
                    finalSql.append(" ").append(alias.toLowerCase()).append(" ");
                }
            }
        }

        if (joinType != SQLJoinTableSource.JoinType.COMMA) {
            // 非 逗号 连接
            finalSql.append(" ON ").append(whereCondition.toString()).append(" ");
        }

    }

    /**
     * 处理join的select *
     */
    private void handleJoinSelectAll(Map<String, Set<String>> securityDataMap, SQLJoinTableSource joinCluse,
                                     Set<String> tableAliasSet, StringBuilder finalSql, DatabaseVisitor visitor,
                                     Set<String> tableSet) throws Exception {

        // 处理左连接的字段
        SQLTableSource left = joinCluse.getLeft();
        if (left instanceof SQLJoinTableSource) {
            handleJoinSelectAll(securityDataMap, (SQLJoinTableSource) left, tableAliasSet, finalSql, visitor,
                    tableSet);
        } else {
            handleCommonSelectAll(securityDataMap, tableAliasSet, left, finalSql, visitor, tableSet);
        }

        finalSql.append(" , ");

        // 处理右连接的字段
        SQLTableSource right = joinCluse.getRight();
        if (right instanceof SQLJoinTableSource) {
            handleJoinSelectAll(securityDataMap, (SQLJoinTableSource) right, tableAliasSet, finalSql, visitor, tableSet);
        } else {
            handleCommonSelectAll(securityDataMap, tableAliasSet, right, finalSql, visitor, tableSet);
        }
    }

    private void handleJoinTables(SQLJoinTableSource joinCluse, List<TableNameInfo> tableNames, String dbType,
                                  Set<String> tableSet) {
        TableNameInfo tableName = null;
        // 左侧
        tableName = new TableNameInfo();
        SQLTableSource leftJoin = joinCluse.getLeft();
        if (leftJoin instanceof SQLJoinTableSource) {
            // 是一个join
            handleJoinTables((SQLJoinTableSource) leftJoin, tableNames, dbType, tableSet);
        } else if (leftJoin instanceof SQLSubqueryTableSource) {
            // 是一个子查询
            SQLSubqueryTableSource subQuery = (SQLSubqueryTableSource) leftJoin;
            StringBuilder subStr = new StringBuilder();
            SQLASTOutputVisitor visitor = SqlUtils.createFormatOutputVisitor(subStr, dbType);
            subQuery.accept(visitor);
            if (!subStr.toString().startsWith("(")) {
                subStr.insert(0, "(");
                subStr.append("");
            }

            // 得到该子查询的select
            StringBuilder selectStr = new StringBuilder(subStr.toString());
            selectStr.delete(0, selectStr.indexOf("(") + 1);
            selectStr.delete(selectStr.lastIndexOf(")"), selectStr.length());
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(selectStr.toString(), dbType);
            List<SQLStatement> stmtList = parser.parseStatementList();

            SQLSelectStatement stmt = (SQLSelectStatement) stmtList.get(0);
            tableName.setSelect(stmt.getSelect());

            tableName.setAlias(subQuery.getAlias());
            tableName.setName(subStr.toString());
            tableName.setNestQuery(true);
            tableNames.add(tableName);
        } else {
            SQLExprTableSource sqlExpr = (SQLExprTableSource) leftJoin;
            tableName.setName(sqlExpr.getExpr().toString());
            tableName.setAlias(sqlExpr.getAlias());
            tableName.setNestQuery(false);
            tableNames.add(tableName);

            tableSet.add(sqlExpr.getExpr().toString().toUpperCase());
        }


        // 右侧
        tableName = new TableNameInfo();
        SQLTableSource rightJoin = joinCluse.getRight();
        if (rightJoin instanceof SQLJoinTableSource) {
            // 是一个join
            handleJoinTables((SQLJoinTableSource) rightJoin, tableNames, dbType, tableSet);
        } else if (rightJoin instanceof SQLSubqueryTableSource) {
            // 是一个子查询
            SQLSubqueryTableSource subQuery = (SQLSubqueryTableSource) rightJoin;
            StringBuilder subStr = new StringBuilder();
            SQLASTOutputVisitor visitor = SqlUtils.createFormatOutputVisitor(subStr, dbType);
            subQuery.accept(visitor);
            if (!subStr.toString().startsWith("(")) {
                subStr.insert(0, "(");
                subStr.append("");
            }
            tableName.setAlias(subQuery.getAlias());
            tableName.setName(subStr.toString());
            tableName.setNestQuery(true);
            tableNames.add(tableName);
        } else {
            SQLExprTableSource sqlExpr = (SQLExprTableSource) rightJoin;
            tableName.setName(sqlExpr.getExpr().toString());
            tableName.setAlias(sqlExpr.getAlias());
            tableName.setNestQuery(false);
            tableNames.add(tableName);

            tableSet.add(sqlExpr.getExpr().toString().toUpperCase());
        }

    }

    /**
     * 查询 * 对应的具体字段
     */
    private void handleCommonSelectAll(Map<String, Set<String>> securityDataMap, Set<String> tableAliasSet,
                                       SQLTableSource tableSource, StringBuilder finalSql, DatabaseVisitor visitor,
                                       Set<String> tableSet) throws Exception {
        boolean isSingleTable = false;
        String tableAlias = tableSource.getAlias();
        String tableName = null;

        StringBuilder tableStr = new StringBuilder();
        if (tableSource instanceof SQLExprTableSource) {
            // 是一个单一的表
            tableName = ((SQLExprTableSource) tableSource).getExpr().toString();
            isSingleTable = true;
        } else {
            // 是嵌套查询
            SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(tableStr, this.getDbType());
            tableSource.accept(outputVisitor);

            if (!tableStr.toString().startsWith("(")) {
                tableName = "(" + tableStr + ")";
            } else {
                tableName = tableStr.toString();
            }
        }

        // 不管是单表,还是嵌套查询,要从中获取* 对应的具体字段,构造查询结果为空的sql,只获取表头
        String queryFieldSql = "select * from " + tableName + " where 1=0 ";
        List<String> columns = visitor.getQueryColumnLabels(queryFieldSql);

        if (isSingleTable) {
            // 是单表,要脱敏
            maskColumns(securityDataMap, tableName, tableAlias, columns, finalSql, FROM_TYPE_NORMAL);
        } else {
            // 直接拼装
            // 只有嵌套查询才需生成新的别名, 单表的没有别名可以直接用其表名(除了dual)
            String newAlias = SqlUtils.renameColumn(tableAliasSet, tableName, tableAlias);
            joinTables.put(tableStr.toString().toUpperCase().replaceAll("\\s", ""), newAlias);
            finalSql.append(" ");
            for (String col : columns) {
                if (null != newAlias) {
                    finalSql.append(newAlias.toLowerCase()).append(".");
                }
                finalSql.append(col.toLowerCase()).append(", ");
            }
            finalSql.delete(finalSql.lastIndexOf(", "), finalSql.length());
        }
    }

    /**
     * 处理操作符类型
     */
    private void handleSQLBinaryOpExpr(Map<String, Set<String>> securityDataMap, Set<String> tableAliasSet,
                                       SQLBinaryOpExpr expr, SQLTableSource fromClause, StringBuilder finalSql,
                                       DatabaseVisitor visitor, int fromType,
                                       int exprType, Set<String> tableSet) throws Exception {
        // 左侧
        SQLExpr leftExpr = expr.getLeft();
        // 有运算符
        if (leftExpr instanceof SQLBinaryOpExpr) {
            finalSql.append(" (");
            handleSQLBinaryOpExpr(securityDataMap, tableAliasSet, (SQLBinaryOpExpr) leftExpr, fromClause, finalSql,
                    visitor, fromType, exprType, tableSet);
            finalSql.append(") ");
        } else {
            handleExpr(securityDataMap, visitor, tableAliasSet, leftExpr, null, fromClause, finalSql, fromType,
                    exprType, tableSet);
        }


        // 操作符
        SQLBinaryOperator operator = expr.getOperator();
        finalSql.append(operator.getName());

        // 右侧
        SQLExpr ringhtExpr = expr.getRight();
        // 有运算符
        if (ringhtExpr instanceof SQLBinaryOpExpr) {
            finalSql.append(" (");
            handleSQLBinaryOpExpr(securityDataMap, tableAliasSet, (SQLBinaryOpExpr) ringhtExpr, fromClause,
                    finalSql, visitor, fromType, exprType, tableSet);
            finalSql.append(") ");
        } else {
            handleExpr(securityDataMap, visitor, tableAliasSet, ringhtExpr, null, fromClause, finalSql,
                    fromType, EXPR_TYPE_METHOD, tableSet);
        }
    }

    protected void sqlAggregateMethodWrapSecurityData(Map<String, Set<String>> securityDataMap, DatabaseVisitor visitor,
                                                      Set<String> tableAliasSet, SQLAggregateExpr expr, SQLTableSource fromClause,
                                                      StringBuilder finalSql, Set<String> tableSet) throws Exception {
        finalSql.append(" ").append(expr.getMethodName()).append("(");
        String methodName = expr.getMethodName();

        int fromType = FROM_TYPE_NORMAL;
        if (fromClause instanceof SQLSubqueryTableSource) {
            fromType = FROM_TYPE_NEST;
        } else if (fromClause instanceof SQLJoinTableSource) {
            fromType = FROM_TYPE_JOIN;
        }

        StringBuffer tableStr = new StringBuffer();
        SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(tableStr, this.getDbType());
        fromClause.accept(outputVisitor);

        List<SQLExpr> params = expr.getArguments();

        boolean hasParams = false;
        for (SQLExpr paramExpr : params) {
            hasParams = true;
            if (methodName.equalsIgnoreCase("count")) {
                // 聚合函数内的不需要脱敏
                // 不需要转换*
                handleExpr(securityDataMap, visitor, tableAliasSet, paramExpr, null, fromClause, finalSql, fromType,
                        EXPR_TYPE_METHOD_COUNT, tableSet);
            } else {
                // 不需要加别名
                handleExpr(securityDataMap, visitor, tableAliasSet, paramExpr, null, fromClause, finalSql, fromType,
                        EXPR_TYPE_METHOD, tableSet);
            }
            finalSql.append(", ");
        }
        try {
            if (hasParams) {
                finalSql.delete(finalSql.lastIndexOf(", "), finalSql.length());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        finalSql.append(") ");

        // 处理over部分
        SQLOver over = expr.getOver();
        if (null != over) {
            StringBuilder overStr = new StringBuilder();
            outputVisitor = SqlUtils.createFormatOutputVisitor(overStr, this.getDbType());
            over.accept(outputVisitor);
            finalSql.append(" OVER(").append(overStr.toString()).append(") ");
        }
    }

    /**
     * 方法或函数中是否包裹着敏感数据字段， 处理特殊的方法调用，比如Oracle里的字段合并方法WMSYS.WM_concat，也会存在字段脱敏的问题
     */
    protected void methodWrapSecurityData(Map<String, Set<String>> securityDataMap, DatabaseVisitor visitor,
                                          Set<String> tableAliasSet, SQLMethodInvokeExpr expr, SQLTableSource fromClause,
                                          StringBuilder finalSql, Set<String> tableSet) throws Exception {
        int fromType = FROM_TYPE_NORMAL;
        if (fromClause instanceof SQLSubqueryTableSource) {
            fromType = FROM_TYPE_NEST;
        } else if (fromClause instanceof SQLJoinTableSource) {
            fromType = FROM_TYPE_JOIN;
        }

        StringBuffer tableStr = new StringBuffer();
        SQLASTOutputVisitor outputVisitor = SqlUtils.createFormatOutputVisitor(tableStr, this.getDbType());
        fromClause.accept(outputVisitor);

        if ("trim()".equalsIgnoreCase(expr.toString())) {
            finalSql.append(" trim(");
            SQLExpr tmpExpr = (SQLExpr) expr.getAttribute("trim_character");
            handleExpr(securityDataMap, visitor, tableAliasSet, tmpExpr, null, fromClause, finalSql, fromType,
                    EXPR_TYPE_METHOD, tableSet);
            finalSql.append(") ");
            return;
        }

        finalSql.append(" ");

        SQLExpr mOwer = expr.getOwner();
        if (null != mOwer) {
            finalSql.append(mOwer.toString().toLowerCase()).append(".");
        }
        finalSql.append(expr.getMethodName().toLowerCase()).append("(");

        List<SQLExpr> params = expr.getParameters();

        boolean hasParams = false;
        for (SQLExpr paramExpr : params) {
            hasParams = true;
            // 不需要加别名
            handleExpr(securityDataMap, visitor, tableAliasSet, paramExpr, null, fromClause, finalSql, fromType,
                    EXPR_TYPE_METHOD, tableSet);
            finalSql.append(", ");
        }
        try {
            if (hasParams) {
                finalSql.delete(finalSql.lastIndexOf(", "), finalSql.length());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        finalSql.append(") ");
    }


    /**
     * 对敏感字段脱敏
     **/
    private void maskColumns(Map<String, Set<String>> securityDataMap, String tableName, String tableAlias,
                             List<String> columns, StringBuilder finalSql, int fromType) {
        // 根据tableName获取敏感字段，遍历columns改变敏感字段查询形式，变成 MASK_STRING
        boolean hasFound = false;
        for (String colName : columns) {
            hasFound = false;

            Set<String> secColumns = securityDataMap.get(this.getPureTableName(tableName));
            if (secColumns != null) {
                for (String secCol : secColumns) {
                    if (colName.equalsIgnoreCase(secCol)) {
                        finalSql.append(" '").append(MASK_STRING).append("' AS ").append(colName.toLowerCase()).append(", ");
                        if (StringUtils.isNotEmpty(tableAlias)) {
                            finalSql.append(tableAlias.toLowerCase()).append(".");
                        }
                        finalSql.append(colName.toLowerCase()).append(" AS ")
                                .append(HIDDEN_PREFIX).append(colName.toLowerCase());
                        hasFound = true;
                        break;
                    }
                }
            }

            if (!hasFound) {
                if (tableAlias != null) {
                    finalSql.append(tableAlias.toLowerCase()).append(".");
                } else if (!"dual".equalsIgnoreCase(tableName)) {
                    finalSql.append(tableName.toLowerCase()).append(".");
                }
                finalSql.append(colName.toLowerCase());
            }

            finalSql.append(", ");
        }
        try {
            finalSql.delete(finalSql.lastIndexOf(", "), finalSql.length());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 对敏感字段脱敏
     */
    private void maskColumn(Map<String, Set<String>> securityDataMap, String columnName, String columnAlias,
                            String tableName, String tableAlias, StringBuilder finalSql, int fromType, int exprType,
                            SQLAggregateExpr parentAggExpr) {

        finalSql.append(" ");

        boolean hasFound = false;
        Set<String> secColumns = securityDataMap.get(this.getPureTableName(tableName));

        if (tableAlias == null) {
            tableAlias = tableName;
        }

        // 如果是聚合函数内的，可以忽略脱敏，否则转换后语法会出错
        if (secColumns != null && parentAggExpr == null) {
            for (String secCol : secColumns) {
                if (columnName.equalsIgnoreCase(secCol)) {
                    hasFound = true;

                    if (exprType == EXPR_TYPE_NORMAL) {
                        finalSql.append(" '").append(MASK_STRING).append("' ");
                        if (columnAlias != null) {
                            finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(", ");
                            if (StringUtils.isNotEmpty(tableAlias)) {
                                finalSql.append(tableAlias.toLowerCase()).append(".");
                            }
                            finalSql.append(columnName.toLowerCase())
                                    .append(" AS ").append(HIDDEN_PREFIX).append(columnAlias.toLowerCase());
                        } else {
                            finalSql.append(" AS ").append(columnName.toLowerCase()).append(", ");
                            if (StringUtils.isNotEmpty(tableAlias)) {
                                finalSql.append(tableAlias.toLowerCase()).append(".");
                            }
                            finalSql.append(columnName.toLowerCase())
                                    .append(" AS ").append(HIDDEN_PREFIX).append(columnName.toLowerCase());
                        }
                    } else if (exprType == EXPR_TYPE_CASE_WHEN) {
                        // case条件， 不做处理
                        finalSql.append(columnName.toLowerCase()).append(" ");
                    } else if (exprType == EXPR_TYPE_CASE_RESULT || exprType == EXPR_TYPE_METHOD) {
                        // case条件 结果， 不做别名处理
                        finalSql.append(" '").append(MASK_STRING).append("' ");
                    }

                    break;
                }
            }
        }

        if (!hasFound) {

            if (parentAggExpr != null && parentAggExpr.getOption() == SQLAggregateOption.DISTINCT) {
                finalSql.append("DISTINCT ");
            }

            if (!"dual".equalsIgnoreCase(tableAlias) && !("rownum".equalsIgnoreCase(columnName)
                    || "rowid".equalsIgnoreCase(columnName))) {
                finalSql.append(tableAlias.toLowerCase()).append(".");
            }
            finalSql.append(columnName.toLowerCase()).append(" ");

            if (exprType == EXPR_TYPE_NORMAL) {
                if (columnAlias != null) {
                    // 先添加用户指定的别名,没有在添加改变的
                    finalSql.append(" AS ").append(columnAlias.toLowerCase()).append(" ");
                } else if ("rowid".equalsIgnoreCase(columnName)) {
                    finalSql.append(" AS \"row_id\"");
                } else if ("rownum".equalsIgnoreCase(columnName)) {
                    finalSql.append(" AS \"row_num\"");
                }
            }
        }
    }

    private String getPureTableName(String name) {
        List<String> strings = Splitter.on('.').omitEmptyStrings().trimResults().splitToList(name.toUpperCase());
        return strings.get(strings.size() - 1);
    }

    private void maskColumn(Map<String, Set<String>> securityDataMap, String columnName, String columnAlias,
                            String tableName, String tableAlias, StringBuilder finalSql, int fromType, int exprType) {
        maskColumn(securityDataMap, columnName, columnAlias, tableName, tableAlias, finalSql, fromType, exprType, null);
    }

    /**
     * 对最外层的查询字段进行重名处理，保证最终查询的字段名字都是唯一的
     */
    protected String autoRename(String sql) {
        try {
            StringBuilder finalSql = new StringBuilder();
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, this.getDbType());
            List<SQLStatement> stmtList = parser.parseStatementList();
            if (stmtList == null || stmtList.size() < 1) {
                return sql;
            }

            SQLSelectStatement stmt = (SQLSelectStatement) stmtList.get(0);
            SQLSelect select = stmt.getSelect();
            SqlUtils.handleFirstSelectColumns(getDbType(), select, select.getQuery(), finalSql);
            return finalSql.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return sql;
        }
    }

    public abstract String getDbType();

    /**
     * 追加sql中特殊的东西， 比如sql server的top N
     */
    protected abstract void appendSpecial(DatabaseVisitor visitor, SQLSelect sqlselect, StringBuilder finalSql);

    /**
     * 生成数据库个性化的部分，比如MySQL的limit子句
     */
    protected abstract void generateSQLLeftIndividualPart(DatabaseVisitor visitor, SQLSelect sqlselect, StringBuilder finalSQL,
                                                SQLSelectQueryBlock query);
}
