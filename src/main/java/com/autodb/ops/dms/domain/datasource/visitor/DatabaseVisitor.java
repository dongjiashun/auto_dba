package com.autodb.ops.dms.domain.datasource.visitor;

import com.autodb.ops.dms.common.Pair;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Database Visitor
 *
 * @author dongjs
 * @since 2015/12/28
 */
public interface DatabaseVisitor {
    void init(ConnectionInfo info) throws SQLException;
    void refresh(ConnectionInfo info) throws SQLException;
    void close();
    boolean isActive();

    DataSource getDataSource();
    Connection getConnection() throws SQLException;
    Pair<Boolean, String> testConnection(ConnectionInfo info);

    // get
    List<String> getTableNames() throws SQLException;
    Map<String, Object> getTableInfo(String table) throws SQLException;
    List<Map<String, Object>> getTableIndex(String table) throws SQLException;
    List<Map<String, Object>> getTableStruct(String table) throws SQLException;
    String getTableCreateSql(String table) throws SQLException;
    long getTableSize(String table) throws SQLException;

    List<String> getViewNames() throws SQLException;
    Map<String, Object> getViewInfo(String view) throws SQLException;
    List<Map<String, Object>> getViewIndex(String view) throws SQLException;
    List<Map<String, Object>> getViewStruct(String view) throws SQLException;

    List<String> getFunctionNames() throws SQLException;
    List<String> getProcedureNames() throws SQLException;
    List<String> getTriggerNames() throws SQLException;
    String getVariable(String name) throws SQLException;
    Map<String, String> getVariables(String like) throws SQLException;

    List<PrimaryKey> getPrimaryKeys(String table) throws SQLException;
    boolean isView(String tableName) throws SQLException;
    String getViewCreateSql(String view) throws SQLException;
    List<String> getQueryColumnLabels(String sql) throws SQLException;

    // query
    long queryCount(String sql) throws SQLException;
    long queryCount(String sql, Object... params) throws SQLException;
    Result query(String sql) throws SQLException;
    Result query(String sql, int offset, int limit) throws SQLException;
    Result query(String sql, int offset, int limit, Object... params) throws SQLException;


    int update(String sql) throws SQLException;
    int update(Connection conn, String sql) throws SQLException;
    int update(String sql, Object... params) throws SQLException;
    int update(Connection conn, String sql, Object... params) throws SQLException;

    int delete(String sql) throws SQLException;
    int delete(Connection conn, String sql) throws SQLException;
    int delete(String sql, Object... params) throws SQLException;
    int delete(Connection conn, String sql, Object... params) throws SQLException;
}
