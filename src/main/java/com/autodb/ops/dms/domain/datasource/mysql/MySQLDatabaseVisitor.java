package com.autodb.ops.dms.domain.datasource.mysql;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.util.SqlUtils;
import com.autodb.ops.dms.domain.datasource.visitor.AbstractDatabaseVisitor;
import com.autodb.ops.dms.domain.datasource.visitor.ConnectionInfo;
import com.autodb.ops.dms.domain.datasource.visitor.HeaderMapListHandler;
import com.autodb.ops.dms.domain.datasource.visitor.PrimaryKey;
import com.autodb.ops.dms.domain.datasource.visitor.Result;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MySql DatabaseVisitor
 * @author dongjs
 * @since 2015/12/28
 */
public class MySQLDatabaseVisitor extends AbstractDatabaseVisitor {
    private static final String DRIVER = "com.mysql.jdbc.Driver";

    @Override
    public void init(ConnectionInfo info) throws SQLException {
        info.setDriver(DRIVER);
        String connectionUrl = "jdbc:mysql://" + info.getHost() + ":" + info.getPort() + "/" + info.getDatabase()
                + "?zeroDateTimeBehavior=round&useUnicode=true&characterEncoding=utf8&tinyInt1isBit=false";
        info.setConnectionUrl(connectionUrl);
        info.setValidationQuery("select 1");
        info.setConnectionProperties("connectTimeout=" + ConnectionInfo.POOL_CONNECT_TIMEOUT
                + ";socketTimeout=" + ConnectionInfo.POOL_CONNECT_TIMEOUT);

        this.connectionInfo = info;
        initDataSource();
    }

    @Override
    public Pair<Boolean, String> testConnection(ConnectionInfo info) {
        String url = "jdbc:mysql://" + info.getHost() + ":" + info.getPort() + "/" + info.getDatabase();
        return testConnection(DRIVER, url, info.getUsername(), info.getPassword());
    }

    @Override
    public Map<String, Object> getTableInfo(String table) throws SQLException {
        return this.getQueryRunner().query("SELECT TABLE_NAME, TABLE_ROWS, DATA_LENGTH + INDEX_LENGTH AS DATA_LENGTH, "
                + "TABLE_TYPE, AUTO_INCREMENT, ENGINE, SUBSTRING_INDEX(TABLE_COLLATION, '_', 1) `CHARSET`, "
                + "TABLE_COMMENT FROM information_schema.`TABLES` "
                + "WHERE TABLE_SCHEMA=? AND TABLE_NAME=?",
                new MapHandler(), connectionInfo.getDatabase(), table);
    }

    @Override
    public List<Map<String, Object>> getTableIndex(String table) throws SQLException {
        List<Map<String, Object>> index = this.getQueryRunner().query("SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE, NULLABLE, COMMENT "
                + "FROM `INFORMATION_SCHEMA`.`STATISTICS` "
                + "WHERE TABLE_SCHEMA=? AND TABLE_NAME=? ORDER BY INDEX_NAME ASC, SEQ_IN_INDEX ASC",
                new MapListHandler(), connectionInfo.getDatabase(), table);

        // merge index
        Map<String, Object> prefix = null;
        for (Iterator<Map<String, Object>> iterator = index.iterator(); iterator.hasNext();) {
            Map<String, Object> data = iterator.next();
            String indexName = data.get("INDEX_NAME").toString();

            if (null != prefix && indexName.equalsIgnoreCase(prefix.get("INDEX_NAME").toString())) {
                prefix.put("COLUMN_NAME", prefix.get("COLUMN_NAME") + "," + data.get("COLUMN_NAME"));
                iterator.remove();
            } else {
                prefix = data;
            }
        }
        return index;
    }

    @Override
    public List<Map<String, Object>> getTableStruct(String table) throws SQLException {
        return this.getQueryRunner().query("SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_KEY, IS_NULLABLE, "
                + "CASE `EXTRA` WHEN 'auto_increment' THEN 'true' ELSE '' END AUTO_INCREMENT, COLUMN_COMMENT "
                + "FROM information_schema.COLUMNS "
                + "WHERE `TABLE_SCHEMA`=? AND `TABLE_NAME`=? ORDER BY `TABLE_NAME` ASC",
                new MapListHandler(), connectionInfo.getDatabase(), table);
    }

    @Override
    public String getTableCreateSql(String table) throws SQLException {
        Map<String, Object> data = this.getQueryRunner().query("show create table `" + table + "`", new MapHandler());
        return data.getOrDefault("Create Table", "").toString();
    }

    @Override
    public long getTableSize(String table) throws SQLException {
        Map<String, Object> data = this.getQueryRunner().query("SELECT (DATA_LENGTH + INDEX_LENGTH) AS size FROM"
                + " information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?",
                new MapHandler(), connectionInfo.getDatabase(), table);

        return data != null ? ((BigInteger) data.getOrDefault("size", BigInteger.ZERO)).longValue() : 0L;
    }

    @Override
    public Map<String, Object> getViewInfo(String view) throws SQLException {
        Map<String, Object> data = this.getQueryRunner().query("SELECT TABLE_NAME, VIEW_DEFINITION, IS_UPDATABLE, "
                        + "CHECK_OPTION, SECURITY_TYPE, DEFINER FROM information_schema.`VIEWS` "
                        + "WHERE TABLE_SCHEMA=? and TABLE_NAME=?",
                new MapHandler(), connectionInfo.getDatabase(), view);

        // format
        Object viewDefinition = data.get("VIEW_DEFINITION");
        if (viewDefinition != null) {
            Pair<Boolean, String> format = SqlUtils.format(viewDefinition.toString(), "mysql");
            if (format.getLeft()) {
                data.put("VIEW_DEFINITION", format.getRight());
            }
        }
        return data;
    }

    @Override
    public String getVariable(String name) throws SQLException {
        return getVariables(name).get(name);
    }

    @Override
    public Map<String, String> getVariables(String like) throws SQLException {
        return this.getQueryRunner().query("show global variables like ?", new MapListHandler(), like)
                .stream()
                .collect(Collectors.toMap(keyBy("Variable_name"), valueBy("Value")));
    }

    private Function<Map<String, Object>, String> keyBy(String key) {
        return map -> map.get(key).toString();
    }

    private Function<Map<String, Object>, String> valueBy(String key) {
        return map -> {
            Object value = map.get(key);
            return value != null ? value.toString() : null;
        };
    }

    @Override
    public List<PrimaryKey> getPrimaryKeys(String table) throws SQLException {
        List<Map<String, Object>> data = this.getQueryRunner().query("SELECT c.column_name, "
                + "CASE c.`EXTRA` WHEN 'auto_increment' THEN 'true' ELSE 'false' END 'auto_increment' "
                + "FROM information_schema.columns c "
                + "WHERE c.`TABLE_SCHEMA`=? and c.column_key='PRI' and c.table_name=? "
                + "ORDER BY c.column_name ASC",
                new MapListHandler(), connectionInfo.getDatabase(), table);

        return data.stream().map(item -> {
            PrimaryKey primaryKey = new PrimaryKey();
            primaryKey.setName(item.get("column_name").toString());
            primaryKey.setAutoIncrement(Boolean.parseBoolean(item.get("auto_increment").toString()));
            return primaryKey;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isView(String tableName) throws SQLException {
        Map<String, Object> data = this.getQueryRunner().query("SELECT TABLE_NAME, VIEW_DEFINITION, IS_UPDATABLE, "
                        + "CHECK_OPTION, SECURITY_TYPE, DEFINER FROM information_schema.`VIEWS` "
                        + "WHERE TABLE_SCHEMA=? and TABLE_NAME=?",
                new MapHandler(), connectionInfo.getDatabase(), tableName);
        return data != null;
    }

    @Override
    public String getViewCreateSql(String view) throws SQLException {
        Map<String, Object> data = this.getQueryRunner().query("SELECT TABLE_NAME, VIEW_DEFINITION, IS_UPDATABLE, "
                        + "CHECK_OPTION, SECURITY_TYPE, DEFINER FROM information_schema.`VIEWS` "
                        + "WHERE TABLE_SCHEMA=? and TABLE_NAME=?",
                new MapHandler(), connectionInfo.getDatabase(), view);
        return data != null ? data.getOrDefault("VIEW_DEFINITION", "").toString() : "";
    }

    @Override
    public Result query(String sql, int offset, int limit, Object... params) throws SQLException {
        String newSql = "select * from (" + prepareSql(sql) + ") as query_tmp_table limit ?, ?";

        ArrayList<Object> paramsList = new ArrayList<>();
        if (params != null) {
            Collections.addAll(paramsList, params);
        }
        paramsList.add(offset);
        paramsList.add(limit);

        HeaderMapListHandler handler = new HeaderMapListHandler();
        List<Map<String, Object>> data = this.getQueryRunner().query(newSql, handler, paramsList.toArray());

        return new Result(handler.getResultHeader(), data);
    }
}
