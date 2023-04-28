package com.autodb.ops.dms.service.task.impl;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.util.JdbcUtils;
import com.autodb.ops.dms.entity.datasource.DataSourceCobar;
import com.autodb.ops.dms.repository.datasource.DataSourceCobarDao;
import com.google.common.base.Joiner;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.common.util.CsvUtils;
import com.autodb.ops.dms.common.util.SqlFormatUtils;
import com.autodb.ops.dms.common.util.SqlUtils;
import com.autodb.ops.dms.domain.datasource.DataSourceManager;
import com.autodb.ops.dms.domain.datasource.sql.SQLService;
import com.autodb.ops.dms.domain.datasource.visitor.ConnectionInfo;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.domain.datasource.visitor.PrimaryKey;
import com.autodb.ops.dms.domain.datasource.visitor.Result;
import com.autodb.ops.dms.domain.flyway.FlywayService;
import com.autodb.ops.dms.dto.task.DataChangeApply;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.task.DataChange;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.task.DataChangeDao;
import com.autodb.ops.dms.service.task.DataChangeService;
import com.google.common.collect.Maps;
import lombok.Data;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DataChangeService Impl
 *
 * @author dongjs
 * @since 16/1/25
 */
@Service
public class DataChangeServiceImpl extends AbstractTaskService implements DataChangeService {
    private static Logger log = LoggerFactory.getLogger(DataChangeServiceImpl.class);

    private static final String PROCESS_DEFINITION_KEY = "data-change";

    @Value("${data.change.max-size}")
    private int maxChangeSize = 1000;

    @Autowired
    private DataChangeDao dataChangeDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    DataSourceCobarDao dataSourceCobarDao;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private FlywayService flywayService;

    @Override
    @Transactional
    public Pair<Integer, String> apply(User user, DataChangeApply dataChangeApply) throws AppException {
        try {
            // validate ds
            Map<Integer, DataSource> dataSourceMap = dataSourceDao.findAuthByUser(user.getId()).stream()
                    .collect(Collectors.toMap(DataSource::getId, dataSource -> dataSource));
            dataChangeApply.getChanges().forEach(change -> change.setDataSource(dataSourceMap.get(change.getDs())));

            List<DataSource> dataSourceList = dataChangeApply.getChanges().stream()
                    .filter(change -> change.getDataSource() != null)
                    .map(DataChangeApply.Change::getDataSource)
                    .collect(Collectors.toList());

            // exists unknown ds
            if (dataSourceList.size() != dataChangeApply.getChanges().size()) {
                return Pair.of(1, null);
            }

            List<Integer> dsIdList = dataSourceList.stream().map(DataSource::getId).collect(Collectors.toList());
            List<String> dsNameList = dataSourceList.stream().map(DataSource::getName).collect(Collectors.toList());

            for (DataChangeApply.Change change : dataChangeApply.getChanges()) {
                Triple<Boolean, List<Pair<String, String>>, String> selects = SqlUtils
                        .dataChangeStatements(change.getSql(), change.getDataSource().getType());
                if (!selects.getLeft()) {
                    return Pair.of(2, selects.getRight());
                }
            }

            // task biz
            TaskBiz taskBiz = new TaskBiz();
            taskBiz.setType(TaskBiz.Type.DATA_CHANGE);
            taskBiz.setStartUser(user);
            taskBiz.setStatus(TaskBiz.Status.PROCESS);
            taskBiz.setStartTime(new Date());
            taskBiz.setInfo(Joiner.on(',').join(dsNameList));
            taskBiz.setExplain(dataChangeApply.getTitle());
            taskBizDao.add(taskBiz);

            Map<String,String> dsName = Maps.newHashMap();
            Map<String,String> dsEnvs = Maps.newHashMap();

            dataChangeApply.getChanges().forEach(change -> {
                DataChange dataChange = new DataChange();
                DataSource ds = change.getDataSource();

                dsName.put(ds.getId()+"",ds.getName());
                dsEnvs.put(ds.getId()+"",ds.getEnv());

                dataChange.setTask(taskBiz);
                dataChange.setKey(ds.getId().toString());
                dataChange.setDsEnv(ds.getEnv());
                dataChange.setDsName(ds.getName());
                dataChange.setReason(dataChangeApply.getReason());
                dataChange.setSql(change.getSql());
                dataChangeDao.add(dataChange);
            });

            Map<String, Object> variables = new HashMap<>();
            variables.put("applyUser", user.getUsername());
            variables.put("dsList", dsIdList);
            variables.put("dsName", dsName);
            variables.put("dsEnvs", dsEnvs);
            variables.put("title", dataChangeApply.getTitle());
            //activiti工作流
            identityService.setAuthenticatedUserId(user.getUsername());
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                    taskBiz.getId().toString(), variables);

            taskBizDao.updateProcessInstanceId(taskBiz.getId(), processInstance.getProcessInstanceId());
            taskBiz.setProcessInstanceId(processInstance.getProcessInstanceId());

            return Pair.of(0, null);
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional
    public Pair<Integer, String> approve(String id, User assessor, boolean agree, boolean backup, String reason) throws AppException {
        try {
            Pair<Integer, String> result;
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(PROCESS_DEFINITION_KEY)
                    .taskDefinitionKey("audit")
                    .taskId(id)
                    .singleResult();

            if (task != null) {
                Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
                String ds = variables.get("ds").toString();
                TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                DataChange dataChange = dataChangeDao.findByTask(taskBiz.getId(), ds);

                if (agree) {
                    DataSource dataSource = dataSourceDao.find(ds);
                    dataChange(dataSource, dataChange, backup);
                    if (!dataChange.isExecuteSuccess()) {
                        return Pair.of(1, dataChange.getMessage());
                    } else {
                        flywayService.dataChange(dataSource, taskBiz.getExplain(), dataChange);
                    }
                }

                dataChange.setAssessor(assessor.getNickname());
                dataChange.setAssessType(agree ? DataChange.AssessType.AGREE : DataChange.AssessType.REJECT);
                dataChange.setAssessTime(new Date());
                dataChange.setAssessRemark(reason);
                dataChangeDao.update(dataChange);

                taskService.claim(id, assessor.getUsername());
                taskService.addComment(id, task.getProcessInstanceId(), reason);

                Map<String, Object> vars = new HashMap<>();
                vars.put("approved", agree);
                vars.put("comment", reason);
                vars.put("assessorUser",assessor.getUsername());
                taskService.complete(id, vars);
                result = Pair.of(0, dataChange.getMessage());
            } else {
                result = Pair.of(1, "task not find");
            }

            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("data-change approve error", e);
            return Pair.of(1, "task not find");
        }
    }

    /**
     * data change
     * <ul>
     *  <li>检查数据源</li>
     *  <li>检查变更阈值</li>
     *  <li>检查主键,给出没有主键的警告</li>
     *  <li>生成回滚数据</li>
     *  <li>生成回滚sql</li>
     * </ul>
     */
    protected void dataChange(DataSource dataSource, DataChange dataChange, boolean backup) throws AppException {
        try {
            // 检查数据源
            if (dataSource != null) {
                Triple<Boolean, List<Pair<String, String>>, String> selects = SqlUtils
                        .dataChangeStatements(dataChange.getSql(), dataSource.getType());
                if (selects.getLeft()) {
                    DataChangeBo dataChangeBo = DataChangeBo.of(dataSource, dataChange, selects.getMiddle(), backup);

                    backupSql(dataChangeBo);
                    // 检查变更阈值
                    checkMaxSize(dataChangeBo);

                    if (dataChangeBo.isBackup()) {
                        // 检查主键,给出没有主键的警告
                        checkPrimaryKeys(dataChangeBo);
                        // 生成回滚数据
                        backupData(dataChangeBo);
                        // 生成回滚sql
                        backupRollbackSql(dataChangeBo);
                    }
                    // 执行变更
                    execDataChange(dataChangeBo);
                } else {
                    failDataChange(dataChange, selects.getRight());
                }
            } else {
                failDataChange(dataChange, "数据源不存在");
            }
        } catch (Exception e) {
            failDataChange(dataChange, e.getMessage());
        }
    }

    @Override
    @Transactional
    public Pair<Integer, String> adjust(User user, String id, boolean reApply, String reason, String sql) throws AppException {
        try {
            Pair<Integer, String> result = Pair.of(0, null);
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(PROCESS_DEFINITION_KEY)
                    .taskDefinitionKey("adjust")
                    .taskId(id)
                    .singleResult();

            if (task != null) {
                if (reApply) {
                    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
                    String ds = variables.get("ds").toString();
                    TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                    DataChange dataChange = dataChangeDao.findByTask(taskBiz.getId(), ds);

                    DataSource dataSource = dataSourceDao.find(dataChange.getKey());
                    Triple<Boolean, List<Pair<String, String>>, String> selects = SqlUtils
                            .dataChangeStatements(sql, dataSource.getType());
                    if (!selects.getLeft()) {
                        return Pair.of(2, selects.getRight());
                    }

                    dataChange.setReason(reason);
                    dataChange.setSql(sql);
                    dataChangeDao.update(dataChange);
                }

                taskService.claim(id, user.getUsername());
                taskService.addComment(id, task.getProcessInstanceId(), reason);
                taskService.complete(id, Collections.singletonMap("reApply", String.valueOf(reApply)));
            } else {
                result = Pair.of(1, null);
            }
            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("data-export adjust error", e);
            return Pair.of(1, null);
        }
    }

    @Override
    @Transactional
    public int result(User user, String id) throws AppException {
        return this.completeTask(user, "result", id);
    }

    @Override
    protected Object findEntityByTask(int taskId) {
        return dataChangeDao.findByTask(taskId);
    }

    @Override
    protected Object findEntityByTask(int taskId, String key) {
        return dataChangeDao.findByTask(taskId, key);
    }

    @Override
    protected String getProcessDefinitionKey() {
        return PROCESS_DEFINITION_KEY;
    }

    // =========================== data change biz ===========================

    /** mark DataChange failed **/
    private void failDataChange(DataChange dataChange, String message) {
        dataChange.setExecuteSuccess(false);
        dataChange.setAffectSize(0);
        dataChange.setMessage(message);
        dataChange.setRollbackSqlFile(null);
        dataChange.setBackupFile(null);
    }

    private void failDataChange(DataChange dataChange, DataChangeBo dataChangeBo, String message) {
        dataChangeBo.setFail(true);

        dataChange.setExecuteSuccess(false);
        dataChange.setAffectSize(0);
        dataChange.setMessage(message);
        dataChange.setRollbackSqlFile(null);
        dataChange.setBackupFile(null);
    }

    private void appendDataChangeMsg(DataChange dataChange, String message) {
        String msg = dataChange.getMessage();
        dataChange.setMessage(msg != null ? msg + message : message);
    }

    private void backupSql(DataChangeBo dataChangeBo) {
        if (dataChangeBo.isFail()) {
            return;
        }

        for (DataChangeSqlBo sqlBo : dataChangeBo.getSql()) {
            String dbType = dataChangeBo.getDataSource().getType();
            StringBuilder backupSql = new StringBuilder();

            SQLStatementParser sqlStatementParser = SQLParserUtils.createSQLStatementParser(sqlBo.getSql(), dbType);
            SQLStatement stmt = sqlStatementParser.parseStatement();

            String sqlType = sqlBo.getType();
            if (SqlUtils.UPDATE.equalsIgnoreCase(sqlType)) {
                backupSql.append("SELECT * FROM ");
                SQLUpdateStatement statement = (SQLUpdateStatement) stmt;
                sqlBo.setTableName(SqlFormatUtils.toOriginalString(statement.getTableName().getSimpleName(), dbType));

                SQLASTOutputVisitor visitor = SqlUtils.createFormatOutputVisitor(backupSql, dbType);
                statement.getTableSource().accept(visitor);

                // append where clause
                backupSql.append(" WHERE ");
                visitor = SqlUtils.createFormatOutputVisitor(backupSql, dbType);
                try {
                    statement.getWhere().accept(visitor);
                } catch (NullPointerException e) {
                    backupSql.delete(backupSql.lastIndexOf("WHERE"), backupSql.length());
                }
            } else if (SqlUtils.DELETE.equalsIgnoreCase(sqlType)) {
                backupSql.append("SELECT * FROM ");
                SQLDeleteStatement statement = (SQLDeleteStatement) stmt;
                sqlBo.setTableName(SqlFormatUtils.toOriginalString(statement.getTableName().getSimpleName(), dbType));

                SQLASTOutputVisitor visitor = SqlUtils.createFormatOutputVisitor(backupSql, dbType);
                statement.getTableSource().accept(visitor);

                // append where clause
                backupSql.append(" WHERE ");
                try {
                    statement.getWhere().accept(visitor);
                } catch (Exception e) {
                    backupSql.delete(backupSql.lastIndexOf("WHERE"), backupSql.length());
                }
            } else if (SqlUtils.INSERT.equalsIgnoreCase(sqlType)) {
                SQLInsertStatement statement = (SQLInsertStatement) stmt;

                List<SQLExpr> columns = statement.getColumns();
                for (SQLExpr expr : columns) {
                    sqlBo.getColumns().add(expr.toString());
                }

                sqlBo.setTableName(SqlFormatUtils.toOriginalString(statement.getTableName().getSimpleName(), dbType));
            }
            sqlBo.setBackupSql(backupSql.toString());
        }
    }

    private void checkMaxSize(DataChangeBo dataChangeBo) throws SQLException {
        if (dataChangeBo.isFail()) {
            return;
        }

        int totalAffectSize = 0;
        String dbType = dataChangeBo.getDataSource().getType();
        DatabaseVisitor visitor = dataSourceManager
                .getDatabaseVisitor(dataChangeBo.getDataSource().backupConnectionInfo());
        for (DataChangeSqlBo sqlBo : dataChangeBo.getSql()) {
            String type = sqlBo.getType();
            if (SqlUtils.DELETE.equalsIgnoreCase(type) || SqlUtils.UPDATE.equalsIgnoreCase(type)) {
                long count = visitor.queryCount(sqlBo.getBackupSql());
                sqlBo.setAffectSize((int) count);
                totalAffectSize += count;
                if (count > maxChangeSize) {
                    failDataChange(dataChangeBo.getDataChange(), dataChangeBo,
                            String.format("%s%s] 涉及 [%d] 条数据变更，超过阈值 [%d]\n", '[', sqlBo.getSql(), count, maxChangeSize));
                    return;
                }
            } else if (SqlUtils.INSERT.equalsIgnoreCase(type)) {
                // insert 的子查询语句
                StringBuffer insertSql = new StringBuffer();
                StringBuffer tableName = new StringBuffer();
                if (SqlUtils.isCopyInsert(dbType, sqlBo.getSql(), insertSql, tableName)) {
                    // 检查插入子查询数据的数量
                    long count = visitor.queryCount(SqlUtils.changeSequenceItemToNull(insertSql.toString(), dbType));
                    sqlBo.setAffectSize((int) count);
                    totalAffectSize += count;
                    if (count > maxChangeSize) {
                        failDataChange(dataChangeBo.getDataChange(), dataChangeBo,
                                String.format("%s%s] 涉及 [%d] 条数据变更，超过阈值 [%d]\n", '[', sqlBo.getSql(), count, maxChangeSize));
                        return;
                    }
                    if (count > 1000) {
                        appendDataChangeMsg(dataChangeBo.getDataChange(), "拷贝插入数据量已经超过预期值：1000条，强烈建议手动执行。\n"
                                + "（警告：如果强制执行，为保证数据一致性，会一次性查询子查询的数据，可能会有内存溢出问题）\n\n");
                    }
                } else {
                    sqlBo.setAffectSize(1);
                    totalAffectSize += 1;
                }
            }
        }
        appendDataChangeMsg(dataChangeBo.getDataChange(), "总共影响的行数 [" + totalAffectSize + "]\n\n");
    }

    private void checkPrimaryKeys(DataChangeBo dataChangeBo) throws SQLException {
        if (dataChangeBo.isFail()) {
            return;
        }


        DatabaseVisitor visitor = dataSourceManager
                .getDatabaseVisitor(dataChangeBo.getDataSource().backupConnectionInfo());

        if (dataChangeBo.getDataSource().isCobar()) {
            DataSourceCobar cobar = this.dataSourceCobarDao.findByDataSource(dataChangeBo.getDataSource());
            if (cobar != null) {
                visitor = this.dataSourceManager.getDatabaseVisitor(cobar.backupConnectionInfo());
            }
        }

        for (DataChangeSqlBo sqlBo : dataChangeBo.getSql()) {
            List<PrimaryKey> primaryKeys = visitor.getPrimaryKeys(sqlBo.getTableName());
            sqlBo.setPrimaryKeys(primaryKeys);

            if (primaryKeys.size() == 0) {
                failDataChange(dataChangeBo.getDataChange(), dataChangeBo, "[" + sqlBo.getSql() + "] --> "
                        + "表[" + sqlBo.getTableName() + "] 没有主键, 将不能生成回滚sql\n");
                return;
            }

            // 检查是否是插入操作,如果是插入的话，mysql必须有自增的主键，sql server不支持插入操作的回滚sql
            if (SqlUtils.INSERT.equalsIgnoreCase(sqlBo.getType())) {
                if (JdbcUtils.MYSQL.equalsIgnoreCase(sqlBo.getType())) {
                    for (PrimaryKey pk : primaryKeys) {
                        if (!pk.isAutoIncrement()) {
                            failDataChange(dataChangeBo.getDataChange(), dataChangeBo, "[" + sqlBo.getSql() + "] --> "
                                    + "表[" + sqlBo.getTableName() + "] 没有自增主键, 将不能生成回滚sql\n");
                        }
                    }
                } else if (JdbcUtils.SQL_SERVER.equalsIgnoreCase(sqlBo.getType())) {
                    failDataChange(dataChangeBo.getDataChange(), dataChangeBo, "[" + sqlBo.getSql() + "] --> "
                            + "表[" + sqlBo.getTableName() + "] 是sql server数据库insert操作，将不能为sql server插入生成回滚sql\n");
                }
            }
        }
    }

    private void backupData(DataChangeBo dataChangeBo) throws SQLException, IOException {
        if (dataChangeBo.isFail()) {
            return;
        }

        DatabaseVisitor visitor = dataSourceManager
                .getDatabaseVisitor(dataChangeBo.getDataSource().backupConnectionInfo());
        List<String> filenames = new ArrayList<>();
        for (DataChangeSqlBo sqlBo : dataChangeBo.getSql()) {
            if (StringUtils.isBlank(sqlBo.getBackupSql())) {
                continue;
            }

            if (sqlBo.getAffectSize() == 0) {
                appendDataChangeMsg(dataChangeBo.getDataChange(), '[' + sqlBo.getSql() + "] 没有查询到数据，未生成备份数据\n\n");
                continue;
            }
            Result backupData = visitor.query(sqlBo.getBackupSql(), 0, maxChangeSize);
            int size = backupData.getData().size();
            if (size == 0) {
                appendDataChangeMsg(dataChangeBo.getDataChange(), '[' + sqlBo.getSql() + "] 没有查询到数据，未生成备份数据\n\n");
            } else {
                // write file
                Pair<String, String> filename = this.getBackupFileName();
                CsvUtils.write2File(filename.getLeft(), Arrays.asList(backupData.getHeader().getColumnNames()),
                        backupData.getData(), null);
                appendDataChangeMsg(dataChangeBo.getDataChange(), "[" + sqlBo.getBackupSql() + "]\n 备份成功, 一共 [" + size + "] 条\n\n");
                filenames.add(filename.getRight());
            }
        }

        dataChangeBo.getDataChange().setBackupFile(Joiner.on(";").join(filenames));
    }

    private void backupRollbackSql(DataChangeBo dataChangeBo) throws SQLException, IOException {
        if (dataChangeBo.isFail()) {
            return;
        }

        StringBuilder rollbackSql = new StringBuilder();
        DatabaseVisitor visitor = dataSourceManager
                .getDatabaseVisitor(dataChangeBo.getDataSource().backupConnectionInfo());
        for (DataChangeSqlBo sqlBo : dataChangeBo.getSql()) {
            String type = sqlBo.getType();
            if (SqlUtils.DELETE.equalsIgnoreCase(type)) {
                rollbackSql.append(backupSQL4Delete(visitor, sqlBo));
            } else if (SqlUtils.UPDATE.equalsIgnoreCase(type)) {
                rollbackSql.append(backupSQL4Update(visitor, sqlBo));
            }
        }

        String sql = rollbackSql.toString();
        writeRollbackSql2File(dataChangeBo, sql);
    }

    /** write or append rollback sql to file **/
    private void writeRollbackSql2File(DataChangeBo dataChangeBo, String sql) throws IOException {
        if (StringUtils.isNotBlank(sql)) {
            String rollbackSqlFile = dataChangeBo.getRollbackSqlFullName();
            if (StringUtils.isEmpty(rollbackSqlFile)) {
                Pair<String, String> filename = this.getSqlFileName();
                FileUtils.writeStringToFile(new File(filename.getLeft()), sql);
                dataChangeBo.setRollbackSqlFullName(filename.getLeft());
                dataChangeBo.getDataChange().setRollbackSqlFile(filename.getRight());
            } else {
                FileUtils.writeStringToFile(new File(rollbackSqlFile), sql, true);
            }
        }
    }

    private void execDataChange(DataChangeBo dataChangeBo) throws SQLException, IOException {
        if (dataChangeBo.isFail()) {
            return;
        }
        DataChange dataChange = dataChangeBo.getDataChange();

        ConnectionInfo connectionInfo = dataChangeBo.getDataSource().mainConnectionInfo();
        DatabaseVisitor visitor = dataSourceManager
                .getDatabaseVisitor(connectionInfo);

        Connection conn = visitor.getConnection();
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            QueryRunner runner = new QueryRunner();
            for (DataChangeSqlBo sqlBo : dataChangeBo.getSql()) {
                String type = sqlBo.getType();
                if (SqlUtils.INSERT.equalsIgnoreCase(type)) {
                    // backup insert
                    SQLService sqlService = dataSourceManager.getSQLService(connectionInfo);
                    String rollbackSql = sqlService.insertAndRollbackSql(conn, sqlBo.getSql(),
                            sqlBo.getTableName(), sqlBo.getPrimaryKeys());

                    // append rollback sql to file
                    writeRollbackSql2File(dataChangeBo, rollbackSql);
                    log.debug("data change insert rollback sql [{}]", rollbackSql);
                } else if (SqlUtils.UPDATE.equalsIgnoreCase(type)) {
                    runner.update(conn, sqlBo.getSql());
                } else if (SqlUtils.DELETE.equalsIgnoreCase(type)) {
                    runner.update(conn, sqlBo.getSql());
                }
            }
            conn.commit();
            dataChange.setExecuteSuccess(true);
            dataChange.setAffectSize(dataChangeBo.getTotalAffectSize());
            log.debug("execute data change commit");
        } catch (Exception e) {
            conn.rollback();
            log.debug("execute data change rollback");
            failDataChange(dataChange, dataChangeBo, e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
            conn.close();
        }
    }

    /**
     * 更新操作对应的回滚sql
     */
    private StringBuilder backupSQL4Delete(DatabaseVisitor visitor, DataChangeSqlBo sqlBo) throws SQLException {
        StringBuilder insertSql = new StringBuilder();
        Result result = visitor.query(sqlBo.getBackupSql(), 0, maxChangeSize);
        if (result.getData().size() > 0) {
            String[] headers = result.getHeader().getColumnNames();
            int[] types = result.getHeader().getColumnTypes();
            List<Map<String, Object>> resultData = result.getData();

            for (Map<String, Object> data : resultData) {
                // start of generate table
                insertSql.append("insert into ").append(sqlBo.getTableName()).append("(");
                // headers
                for (String header : headers) {
                    insertSql.append(SqlFormatUtils.toSqlInsertTitle(header, sqlBo.getType())).append(", ");
                }
                // delete last ","
                insertSql.delete(insertSql.lastIndexOf(","), insertSql.length());
                insertSql.append(") values(");

                for (int i = 0; i < headers.length; i++) {
                    Object obj = data.get(headers[i]);
                    String sqlInsertVal = SqlFormatUtils.toSqlInsertFormat(obj, types[i], sqlBo.getType());
                    insertSql.append(sqlInsertVal).append(", ");
                }
                // delete last ","
                insertSql.delete(insertSql.lastIndexOf(","), insertSql.length());
                insertSql.append(");\n");

            }
            log.debug("generated backup sql of delete is :\n{}\n", insertSql.toString());
            insertSql.append("\n\n");
        }
        return insertSql;
    }

    /**
     * 删除操作对应的回滚sql
     */
    private StringBuilder backupSQL4Update(DatabaseVisitor visitor, DataChangeSqlBo sqlBo) throws SQLException {
        StringBuilder insertSql = new StringBuilder();
        StringBuilder deleteSql = new StringBuilder();
        StringBuilder allSql = new StringBuilder();

        Result result = visitor.query(sqlBo.getBackupSql(), 0, maxChangeSize);
        if (result.getData().size() > 0) {
            String[] headers = result.getHeader().getColumnNames();
            int[] types = result.getHeader().getColumnTypes();
            List<Map<String, Object>> resultData = result.getData();

            // 该表的主键
            List<PrimaryKey> primaryKeys = sqlBo.getPrimaryKeys();
            for (Map<String, Object> data : resultData) {
                insertSql.append("insert into ").append(sqlBo.getTableName()).append("(");
                // header
                for (String header : headers) {
                    insertSql.append(SqlFormatUtils.toSqlInsertTitle(header, sqlBo.getType())).append(", ");
                }
                // delete last ","
                insertSql.delete(insertSql.lastIndexOf(","), insertSql.length());
                insertSql.append(") values(");

                deleteSql.append("delete from ").append(sqlBo.getTableName()).append(" where ");
                for (int j = 0; j < headers.length; j++) {
                    Object obj = data.get(headers[j]);
                    String sqlInsertVal = SqlFormatUtils.toSqlInsertFormat(obj, types[j], sqlBo.getType());
                    insertSql.append(sqlInsertVal).append(", ");

                    // append primary keys to deleteSql
                    for (PrimaryKey pk : primaryKeys) {
                        if (headers[j].equals(pk.getName())) {
                            deleteSql.append(SqlFormatUtils.toSqlInsertTitle(headers[j], sqlBo.getType()))
                                    .append("=").append(sqlInsertVal).append(" and ");
                        }
                    }
                }
                // delete last ","
                insertSql.delete(insertSql.lastIndexOf(","), insertSql.length());
                insertSql.append(");\n");

                deleteSql.delete(deleteSql.lastIndexOf("and"), deleteSql.length());
                deleteSql.append(";\n");

            }
            log.debug("generated backup sql of update is :\n delete first ==================>\n {}"
                    + "\n insert after ====================>\n{}\n", deleteSql.toString(), insertSql.toString());
            allSql.append(deleteSql).append(insertSql).append("\n\n");
        }
        return allSql;
    }

    /** DataChangeBo **/
    @Data
    protected static class DataChangeBo {
        private DataSource dataSource;
        private DataChange dataChange;
        private List<DataChangeSqlBo> sql;
        private String rollbackSqlFullName;
        private boolean backup;

        private boolean fail;

        public static DataChangeBo of(DataSource dataSource, DataChange dataChange,
                                      List<Pair<String, String>> sql, boolean backup) {
            DataChangeBo dataChangeBo = new DataChangeBo();
            dataChangeBo.setDataSource(dataSource);
            dataChangeBo.setDataChange(dataChange);
            dataChangeBo.setSql(sql.stream().map(DataChangeSqlBo::of).collect(Collectors.toList()));
            dataChangeBo.setBackup(backup);
            return dataChangeBo;
        }

        public int getTotalAffectSize() {
            return sql.stream().map(DataChangeSqlBo::getAffectSize).reduce((a, b) -> a + b).get();
        }
    }

    /** DataChangeSqlBo **/
    @Data
    protected static class DataChangeSqlBo {
        private String type;
        private String sql;
        private String backupSql;
        private int affectSize;

        private String tableName;
        private List<String> columns = new ArrayList<>();
        List<PrimaryKey> primaryKeys;

        public static DataChangeSqlBo of(Pair<String, String> sql) {
            DataChangeSqlBo dataChangeSqlBo = new DataChangeSqlBo();
            dataChangeSqlBo.setType(sql.getLeft());
            dataChangeSqlBo.setSql(sql.getRight());
            return dataChangeSqlBo;
        }
    }
}
