package com.autodb.ops.dms.service.task.impl;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.autodb.ops.dms.common.util.SqlFormatUtils;
import com.google.common.base.Joiner;
import com.autodb.ops.dms.common.AppContext;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.common.util.CsvUtils;
import com.autodb.ops.dms.common.util.SqlUtils;
import com.autodb.ops.dms.domain.bi.EncryptionService;
import com.autodb.ops.dms.domain.datasource.DataSourceManager;
import com.autodb.ops.dms.domain.datasource.sql.SQLService;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.domain.datasource.visitor.Result;
import com.autodb.ops.dms.dto.task.DataExportApply;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.task.DataExport;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.task.DataExportDao;
import com.autodb.ops.dms.service.security.SecurityDataService;
import com.autodb.ops.dms.service.task.DataExportService;
import com.google.common.collect.Maps;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataExportService Impl
 *
 * @author dongjs
 * @since 16/1/22
 */
@Service
public class DataExportServiceImpl extends AbstractTaskService implements DataExportService {
    private static Logger log = LoggerFactory.getLogger(DataExportServiceImpl.class);

    private static final String PROCESS_DEFINITION_KEY = "data-export";

    @Value("${data.export.max-size}")
    private int maxExportSize = 1000;

    @Autowired
    private DataExportDao dataExportDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private SecurityDataService securityDataService;

    @Autowired
    private EncryptionService encryptionService;

    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);

    @Override
    @Transactional
    public Pair<Integer, String> apply(User user, DataExportApply dataExportApply) throws AppException {
        try {
            // validate ds
            Map<Integer, DataSource> dataSourceMap = dataSourceDao.findAuthByUser(user.getId()).stream()
                    .collect(Collectors.toMap(DataSource::getId, dataSource -> dataSource));
            dataExportApply.getExports().forEach(export -> export.setDataSource(dataSourceMap.get(export.getDs())));

            List<DataSource> dataSourceList = dataExportApply.getExports().stream()
                    .filter(export -> export.getDataSource() != null)
                    .map(DataExportApply.Export::getDataSource)
                    .collect(Collectors.toList());

            // exists unknown ds
            if (dataSourceList.size() != dataExportApply.getExports().size()) {
                return Pair.of(1, null);
            }

            List<Integer> dsIdList = dataSourceList.stream().map(DataSource::getId).collect(Collectors.toList());
            List<String> dsNameList = dataSourceList.stream().map(DataSource::getName).collect(Collectors.toList());

            for (DataExportApply.Export export : dataExportApply.getExports()) {
                Pair<Boolean, List<String>> selects = SqlUtils
                        .selectStatements(export.getSql(), export.getDataSource().getType());
                if (!selects.getLeft()) {
                    return Pair.of(2, selects.getRight().get(0));
                }
            }

            // task biz
            TaskBiz taskBiz = new TaskBiz();
            taskBiz.setType(TaskBiz.Type.DATA_EXPORT);
            taskBiz.setStartUser(user);
            taskBiz.setStatus(TaskBiz.Status.PROCESS);
            taskBiz.setStartTime(new Date());
            taskBiz.setInfo(Joiner.on(',').join(dsNameList));
            taskBiz.setExplain(dataExportApply.getTitle());
            taskBizDao.add(taskBiz);

            Map<String,String> dsName = Maps.newHashMap();
            Map<String,String> dsEnvs = Maps.newHashMap();

            dataExportApply.getExports().forEach(export -> {
                DataExport dataExport = new DataExport();
                DataSource ds = export.getDataSource();

                dsName.put(ds.getId()+"",ds.getName());
                dsEnvs.put(ds.getId()+"",ds.getEnv());

                dataExport.setTask(taskBiz);
                dataExport.setKey(ds.getId().toString());
                dataExport.setDsEnv(ds.getEnv());
                dataExport.setDsName(ds.getName());
                dataExport.setReason(dataExportApply.getReason());
                dataExport.setSql(export.getSql());
                dataExport.setSecurity(dataExportApply.isSecurity());
                dataExportDao.add(dataExport);
            });

            Map<String, Object> variables = new HashMap<>();
            variables.put("applyUser", user.getUsername());
            variables.put("dsList", dsIdList);
            variables.put("dsName", dsName);
            variables.put("dsEnvs", dsEnvs);
            variables.put("title", dataExportApply.getTitle());
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
    public int approve(String id, User assessor, int agree, String reason, Date execTime) throws AppException {
        try {
            int result = 0;
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(PROCESS_DEFINITION_KEY)
                    .taskDefinitionKey("audit")
                    .taskId(id)
                    .singleResult();

            if (task != null) {
                Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
                String ds = variables.get("ds").toString();
                TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                DataExport dataExport = dataExportDao.findByTask(taskBiz.getId(), ds);

                if (agree < 0 || agree > 2) {
                    agree = 0;
                }
                dataExport.setAssessor(assessor.getNickname());
                dataExport.setAssessType(agree != 0 ? DataExport.AssessType.AGREE : DataExport.AssessType.REJECT);
                dataExport.setAssessTime(new Date());
                dataExport.setAssessRemark(reason);
                dataExportDao.update(dataExport);

                taskService.claim(id, assessor.getUsername());
                taskService.addComment(id, task.getProcessInstanceId(), reason);

                Map<String, Object> vars = new HashMap<>();
                vars.put("approved", agree);
                vars.put("assessorUser", assessor.getUsername());
                if(execTime == null){
                    execTime = new Date();
                }
                DateTime dateTime = new DateTime(execTime.getTime());
                vars.put("executeTime", dateTime.toString("yyyy-MM-dd'T'HH:mm:ss"));
                vars.put("comment", reason);
                taskService.complete(id, vars);
            } else {
                result = 1;
            }

            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("data-export approve error", e);
            return 1;
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
                    DataExport dataExport = dataExportDao.findByTask(taskBiz.getId(), ds);

                    DataSource dataSource = dataSourceDao.find(dataExport.getKey());
                    Pair<Boolean, List<String>> selects = SqlUtils.selectStatements(sql, dataSource.getType());
                    if (!selects.getLeft()) {
                        return Pair.of(2, selects.getRight().get(0));
                    }

                    dataExport.setReason(reason);
                    dataExport.setSql(sql);
                    dataExportDao.update(dataExport);
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
    public int downloadData(User user, String id) throws AppException {
        return this.completeTask(user, "downloadData", id);
    }

    @Override
    @Transactional
    public void export(DelegateExecution execution) throws AppException {
        String ds = execution.getVariable("ds").toString();
        TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(execution.getProcessInstanceId());
        DataExport dataExport = dataExportDao.findByTask(taskBiz.getId(), ds);
        this.dataExport(taskBiz.getStartUser().getUsername(), dataExport);
        dataExportDao.update(dataExport);
    }

    protected void dataExport(String username, DataExport dataExport) throws AppException {
        boolean executeSuccess = false;
        int affectSize = 0;
        String message = "";
        String dataFile = null;

        DataSource dataSource = dataSourceDao.find(dataExport.getKey());
        if (dataSource != null) {
            Pair<Boolean, List<String>> selects = SqlUtils
                    .selectStatements(dataExport.getSql(), dataSource.getType());
            StringBuilder msg = new StringBuilder();
            List<String> filenames = new ArrayList<>();
            try {
                for (String sql : selects.getRight()) {
                    DatabaseVisitor visitor = this.dataSourceManager.getDatabaseVisitor(dataSource.backupConnectionInfo());

                    Result exportData;
                    if (dataExport.isSecurity()) {
                        // 脱敏
                        Set<String> tableSet = new HashSet<>();
                        Map<String, Set<String>> maskData = securityDataService.findMaskData(dataSource.getId(), username);
                        SQLService sqlService = this.dataSourceManager.getSQLService(dataSource.backupConnectionInfo());
                        String newSql = sqlService.securityMaskSql(sql, maskData, visitor, tableSet);
                        exportData = visitor.query(newSql, 0, maxExportSize);
                        encryptionService.encryptResult(exportData, username);//fixme username代替 AppContext.getCurrentUser().getUsername()
                    } else {
                        exportData = visitor.query(sql, 0, maxExportSize);
                    }
                    int size = exportData.getData().size();
                    affectSize += size;
                    if (size == 0) {
                        msg.append('[').append(sql).append("] 没有查询到数据，未生成导出数据\n\n");
                    } else {
                        // write file
                        Pair<String, String> filename = this.getExportFileName();
                        CsvUtils.write2File(filename.getLeft(), Arrays.asList(exportData.getHeader().getColumnNames()),
                                exportData.getData(), sql);
                        msg.append("[").append(sql).append("]\n 导出成功, 一共 [").append(size).append("] 条\n\n");
                        filenames.add(filename.getRight());
                    }

                    //生成对应的sql
                    try {
                        String tempSql = sql.trim();
                        String[] items = tempSql.substring(tempSql.toLowerCase().indexOf("from")).split("\\s");
                        String tableName = items[1];
                        StringBuilder dataSql = backupSQL4Select(exportData, tableName, dataSource.getType());
                        Pair<String, String> filename = this.getSqlFileName();
                        FileUtils.writeStringToFile(new File(filename.getLeft()), dataSql.toString(), true);
                        filenames.add(filename.getRight());
                    }catch (Exception unknown){
                        message = "sql export execute error: " + unknown.getMessage();
                    }
                }


                message = message+msg.toString();
                dataFile = Joiner.on(";").join(filenames);
                executeSuccess = true;
            } catch (SQLException | IOException e) {
                executeSuccess = false;
                affectSize = 0;
                message = "data export execute error: " + e.getMessage();
                dataFile = null;
            }
        } else {
            message = "数据源不存在";
        }

        dataExport.setExecuteSuccess(executeSuccess);
        dataExport.setAffectSize(affectSize);
        dataExport.setMessage(message);
        dataExport.setDataFile(dataFile);
    }

    /**
     * 更新操作对应的回滚sql
     */
    private StringBuilder backupSQL4Select(Result result,String tableName,String type) throws SQLException {
        StringBuilder insertSql = new StringBuilder();
        if (result.getData().size() > 0) {
            String[] headers = result.getHeader().getColumnNames();
            int[] types = result.getHeader().getColumnTypes();
            List<Map<String, Object>> resultData = result.getData();

            for (Map<String, Object> data : resultData) {
                // start of generate table
                insertSql.append("insert into ").append(tableName).append("(");
                // headers
                for (String header : headers) {
                    insertSql.append(SqlFormatUtils.toSqlInsertTitle(header, type)).append(", ");
                }
                // delete last ","
                insertSql.delete(insertSql.lastIndexOf(","), insertSql.length());
                insertSql.append(") values(");

                for (int i = 0; i < headers.length; i++) {
                    Object obj = data.get(headers[i]);
                    String sqlInsertVal = SqlFormatUtils.toSqlInsertFormat(obj, types[i], type);
                    insertSql.append(sqlInsertVal).append(", ");
                }
                // delete last ","
                insertSql.delete(insertSql.lastIndexOf(","), insertSql.length());
                insertSql.append(");\n");

            }
            log.debug("generated select sql of sql is :\n{}\n", insertSql.toString());//这里注意，字符串很长，最长5万条记录拼接，内存大概30*2*5万/（1024*1024） MB
            insertSql.append("\n\n");
        }

        return insertSql;
    }

    @Override
    protected Object findEntityByTask(int taskId) {
        return dataExportDao.findByTask(taskId);
    }

    @Override
    protected Object findEntityByTask(int taskId, String key) {
        return dataExportDao.findByTask(taskId, key);
    }

    @Override
    protected String getProcessDefinitionKey() {
        return PROCESS_DEFINITION_KEY;
    }
}
