package com.autodb.ops.dms.domain.datasource.visitor;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;
import com.autodb.ops.dms.common.Pair;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract DatabaseVisitor<br/>
 * datasource using druid
 * @author dongjs
 * @since 2015/12/28
 */
public abstract class AbstractDatabaseVisitor implements DatabaseVisitor {
    private static Logger logger = LoggerFactory.getLogger(AbstractDatabaseVisitor.class);

    protected DataSource dataSource;
    protected QueryRunner run;

    protected ConnectionInfo connectionInfo;

    @Override
    public void close() {
        if (dataSource != null && dataSource instanceof DruidDataSource) {
            DruidDataSource dataSource = (DruidDataSource) this.dataSource;
            dataSource.close();
            logger.info("close datasource {}", connectionInfo.getConnectionUrl());
        }
    }

    @Override
    public boolean isActive() {
        if (dataSource != null && dataSource instanceof DruidDataSource) {
            DruidDataSource dataSource = (DruidDataSource) this.dataSource;
            return dataSource.getActiveCount() > ConnectionInfo.POOL_MIN_SIZE;
        } else {
            return false;
        }
    }

    @Override
    public void refresh(ConnectionInfo info) throws SQLException {
        logger.info("refresh datasource {}", info.uniqueMark());
        if (!this.connectionInfo.equals(info)) {
            this.init(info);
            logger.info("refresh datasource success {}", connectionInfo.getConnectionUrl());
        } else {
            logger.info("refresh datasource skip");
        }
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<String> getTableNames() throws SQLException {
        return getFeatureNames("TABLE");
    }

    @Override
    public List<String> getViewNames() throws SQLException {
        return getFeatureNames("VIEW");
    }

    @Override
    public List<Map<String, Object>> getViewIndex(String view) throws SQLException {
        return this.getTableIndex(view);
    }

    @Override
    public List<Map<String, Object>> getViewStruct(String view) throws SQLException {
        return getTableStruct(view);
    }

    @Override
    public List<String> getFunctionNames() throws SQLException {
        return getFeatureNames("FUNCTION");
    }

    @Override
    public List<String> getProcedureNames() throws SQLException {
        return getFeatureNames("PROCEDURE");
    }

    @Override
    public List<String> getTriggerNames() throws SQLException {
        return getFeatureNames("TRIGGER");
    }

    @Override
    public List<String> getQueryColumnLabels(String sql) throws SQLException {
        Result result = this.query(sql, 0, 1);
        return Arrays.asList(result.getHeader().getColumnLabels());
    }

    @Override
    public long queryCount(String sql) throws SQLException {
        return this.queryCount(sql, (Object[]) null);
    }

    @Override
    public long queryCount(String sql, Object... params) throws SQLException {
        String newSql = "SELECT count(*) total_count FROM (" + prepareSql(sql) + ") tb";
        HeaderMapListHandler handler = new HeaderMapListHandler();
        List<Map<String, Object>> data = this.getQueryRunner().query(newSql, handler, params);
        if(data.size() > 1){//cobar
            long count = 0;
            for(Map<String, Object> result : data){
                count += (long) result.getOrDefault("total_count", 0L);
            }
            return count;
        }else if(data.size() == 1){//普通的db
            Map<String, Object> result = data.get(0);
            return (long) result.getOrDefault("total_count", 0L);
        }else{//bug
            return 0;
        }
    }

    @Override
    public Result query(String sql) throws SQLException {
        HeaderMapListHandler handler = new HeaderMapListHandler();
        List<Map<String, Object>> data = this.getQueryRunner().query(sql, handler);
        return new Result(handler.getResultHeader(), data);
    }

    @Override
    public Result query(String sql, int offset, int limit) throws SQLException {
        return this.query(sql, offset, limit, (Object[]) null);
    }

    @Override
    public int update(String sql) throws SQLException {
        return this.getQueryRunner().update(sql);
    }

    @Override
    public int update(Connection conn, String sql) throws SQLException {
        return this.getQueryRunner().update(conn, sql);
    }

    @Override
    public int update(String sql, Object... params) throws SQLException {
        return this.getQueryRunner().update(sql, params);
    }

    @Override
    public int update(Connection conn, String sql, Object... params) throws SQLException {
        return this.getQueryRunner().update(conn, sql, params);
    }

    @Override
    public int delete(String sql) throws SQLException {
        return this.update(sql);
    }

    @Override
    public int delete(Connection conn, String sql) throws SQLException {
        return this.update(conn, sql);
    }

    @Override
    public int delete(String sql, Object... params) throws SQLException {
        return this.update(sql, params);
    }

    @Override
    public int delete(Connection conn, String sql, Object... params) throws SQLException {
        return this.update(conn, sql, params);
    }

    protected QueryRunner getQueryRunner() {
        if (run == null) {
            if (dataSource == null) {
                throw new IllegalArgumentException("[Assertion failed] - datasource must not be null");
            }
            synchronized (this) {
                if (run == null) {
                    this.run = new QueryRunner(dataSource);
                }
            }
        }
        return this.run;
    }

    protected List<String> getFeatureNames(String feature) throws SQLException {
        String sql;
        if ("TABLE".equalsIgnoreCase(feature)) {
            sql = "SELECT TABLE_NAME FROM information_schema.TABLES t WHERE t.TABLE_SCHEMA=? ORDER BY TABLE_NAME ASC";
        } else if ("VIEW".equalsIgnoreCase(feature)) {
            sql = "SELECT TABLE_NAME FROM information_schema.VIEWS t WHERE t.TABLE_SCHEMA=? ORDER BY TABLE_NAME ASC";
        } else if ("FUNCTION".equalsIgnoreCase(feature)) {
            sql = "SELECT ROUTINE_NAME FUNCTION_NAME FROM information_schema.ROUTINES "
                    + "WHERE ROUTINE_TYPE='FUNCTION' AND ROUTINE_SCHEMA=? ORDER BY ROUTINE_NAME ASC";
        } else if ("PROCEDURE".equalsIgnoreCase(feature)) {
            sql = "SELECT ROUTINE_NAME PROCEDURE_NAME FROM information_schema.ROUTINES "
                    + "WHERE ROUTINE_TYPE='PROCEDURE' AND ROUTINE_SCHEMA=? ORDER BY ROUTINE_NAME ASC";
        } else if ("TRIGGER".equalsIgnoreCase(feature)) {
            sql = "SELECT TRIGGER_NAME FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA=? ORDER BY TRIGGER_NAME ASC";
        } else {
            throw new IllegalArgumentException("not support feature " + feature);
        }

        List<Object[]> result = getQueryRunner().query(sql, new ArrayListHandler(), connectionInfo.getDatabase());
        return result.stream().map(row -> row[0].toString()).collect(Collectors.toList());
    }

    protected Pair<Boolean, String> testConnection(String driverClassName, String url, String username, String password) {
        try {
            JdbcUtils.createDriver(driverClassName);
            Connection connection = DriverManager.getConnection(url, username, password);
            if (!connection.isClosed()) {
                connection.close();
                return Pair.of(true, "");
            }
            return Pair.of(false, "connection auto closed");
        } catch (SQLException e) {
            return Pair.of(false, e.getMessage());
        }
    }

    protected String prepareSql(String sql) {
        return StringUtils.strip(sql, "; ");
    }

    protected synchronized void initDataSource() throws SQLException {
        if (connectionInfo == null) {
            throw new IllegalArgumentException("[Assertion failed] - connectionInfo must not be null");
        }

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(connectionInfo.getDriver());
        dataSource.setUrl(connectionInfo.getConnectionUrl());
        dataSource.setUsername(connectionInfo.getUsername());
        dataSource.setPassword(connectionInfo.getPassword());

        // 配置初始化大小、最小、最大
        dataSource.setInitialSize(ConnectionInfo.POOL_INIT_SIZE);
        dataSource.setMinIdle(ConnectionInfo.POOL_MIN_SIZE);
        dataSource.setMaxActive(ConnectionInfo.POOL_MAX_SIZE);

        // 配置获取连接等待超时的时间
        dataSource.setMaxWait(ConnectionInfo.POOL_MAX_WAIT);
        // 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        // 配置一个连接在池中最小生存的时间，单位是毫秒
        dataSource.setMinEvictableIdleTimeMillis(300000);

        // 打开PSCache，并且指定每个连接上PSCache的大小
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);

        // 建议配置为true，不影响性能，并且保证安全性。
        // 申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
        dataSource.setTestWhileIdle(true);

        // testWhileIdle的判断依据，详细看testWhileIdle属性的说明
        dataSource.setTimeBetweenEvictionRunsMillis(60000);

        // 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
        dataSource.setTestOnBorrow(true);

        // 校验链接的时候执行的sql
        dataSource.setValidationQuery(connectionInfo.getValidationQuery());

        // 设置socket连接超时时间
        dataSource.setConnectionProperties(connectionInfo.getConnectionProperties());

        dataSource.init();

        if (this.dataSource != null) {
            this.close();
        }
        this.dataSource = dataSource;
        logger.info("init datasource {}", connectionInfo.getConnectionUrl());
    }
}
