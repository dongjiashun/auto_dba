package com.autodb.ops.dms.service.task.impl;

import com.google.common.collect.ImmutableMap;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.domain.datasource.DataSourceManager;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.dto.task.SchemaApplyAdjustForm;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.task.SchemaApply;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.task.SchemaApplyDao;
import com.autodb.ops.dms.service.datasource.DataSourceService;
import com.autodb.ops.dms.service.task.SchemaApplyService;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * SchemaApplyService Impl
 *
 * @author dongjs
 * @since 16/7/26
 */
@Service
public class SchemaApplyServiceImpl extends AbstractTaskService implements SchemaApplyService {
    private static Logger log = LoggerFactory.getLogger(SchemaApplyServiceImpl.class);

    private static final String PROCESS_DEFINITION_KEY = "schema-apply";

    @Autowired
    private SchemaApplyDao schemaApplyDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Override
    @Transactional
    public int apply(User user, SchemaApply schemaApply) throws AppException {
        try {
            DataSource dataSource = dataSourceDao.findByEnvSid(schemaApply.getEnv(), schemaApply.getSid());
            if (dataSource != null) {
                return 1;
            }

            // task biz
            TaskBiz taskBiz = new TaskBiz();
            taskBiz.setType(TaskBiz.Type.SCHEMA_APPLY);
            taskBiz.setStartUser(user);
            taskBiz.setStatus(TaskBiz.Status.PROCESS);
            taskBiz.setStartTime(new Date());
            taskBiz.setInfo(schemaApply.getSid());
            taskBiz.setExplain(schemaApply.getProductDesc());
            taskBizDao.add(taskBiz);

            schemaApply.setTask(taskBiz);
            schemaApplyDao.add(schemaApply);

            identityService.setAuthenticatedUserId(user.getUsername());
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                    taskBiz.getId().toString(),
                    ImmutableMap.of("applyUser", user.getUsername(),
                            "dsEnv", schemaApply.getEnv(), "ds", schemaApply.getSid()));

            taskBizDao.updateProcessInstanceId(taskBiz.getId(), processInstance.getProcessInstanceId());
            taskBiz.setProcessInstanceId(processInstance.getProcessInstanceId());

            return 0;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional
    public Pair<Integer, String> approve(String id, User assessor, byte agree,
                                         int copyDatasourceId, String dsName, String reason)
            throws AppException {
        try {
            Pair<Integer, String> result = Pair.of(0, null);
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(PROCESS_DEFINITION_KEY)
                    .taskDefinitionKey("audit")
                    .taskId(id)
                    .singleResult();

            if (task != null) {
                agree = SchemaApply.toAssessType(agree);
                TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                SchemaApply schemaApply = schemaApplyDao.findByTask(taskBiz.getId());

                // 自动创建
                Pair<Integer, String> addRet = Pair.of(0, null);
                if (SchemaApply.AssessType.AGREE == agree) {
                    addRet = addDataSource(copyDatasourceId, dsName, schemaApply);
                }
                if (addRet.getLeft() == 0) {
                    schemaApply.setAssessor(assessor.getNickname());
                    schemaApply.setAssessType(agree);
                    schemaApply.setAssessTime(new Date());
                    schemaApply.setAssessRemark(reason);
                    schemaApplyDao.update(schemaApply);

                    taskService.claim(id, assessor.getUsername());
                    taskService.addComment(id, task.getProcessInstanceId(), reason);

                    Map<String, Object> vars = new HashMap<>();
                    vars.put("approved", SchemaApply.AssessType.REJECT == agree ? "false" : "true");
                    vars.put("assessorUser",assessor.getUsername());
                    vars.put("comment", reason);
                    taskService.complete(id, vars);
                } else {
                    result = Pair.of(1, addRet.getRight());
                }
            } else {
                result = Pair.of(1, "task not find");
            }
            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("schema-apply approve error", e);
            return Pair.of(1, null);
        }
    }

    @Override
    @Transactional
    public int adjust(User user, String id, SchemaApplyAdjustForm form) throws AppException {
        try {
            int result = 0;
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(PROCESS_DEFINITION_KEY)
                    .taskDefinitionKey("adjust")
                    .taskId(id)
                    .singleResult();

            if (task != null) {
                boolean reApply = form.getApply();
                if (reApply) {
                    TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                    SchemaApply schemaApply = schemaApplyDao.findByTask(taskBiz.getId());

                    schemaApply.setProductDesc(form.getProductDesc());
                    schemaApply.setCapacityDesc(form.getCapacityDesc());
                    schemaApply.setSplit(form.isSplit());
                    schemaApply.setSplitDesc(form.getSplitDesc());
                    schemaApplyDao.update(schemaApply);
                }

                taskService.claim(id, user.getUsername());
                taskService.addComment(id, task.getProcessInstanceId(), "update apply info");
                taskService.complete(id, Collections.singletonMap("reApply", String.valueOf(reApply)));
            } else {
                result = 1;
            }

            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("schema-apply adjust error", e);
            return 1;
        }
    }

    private Pair<Integer, String> addDataSource(int copyDatasourceId, String dsName, SchemaApply schemaApply)
            throws AppException {
        try {
            Pair<Integer, String> result = Pair.of(0, null);
            DataSource dataSource = dataSourceDao.find(copyDatasourceId);
            if (dataSource != null) {
                DatabaseVisitor visitor = dataSourceManager.getDatabaseVisitor(dataSource.mainConnectionInfo());
                // create database
                visitor.update("CREATE DATABASE IF NOT EXISTS " + schemaApply.getSid()
                        + " DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci");

                // hack wait for m-s replication
                TimeUnit.SECONDS.sleep(1);

                // copy and add ds config
                dataSource = copyDataSource(dataSource, dsName, schemaApply);
                Pair<Pair<Boolean, String>, Pair<Boolean, String>> test = dataSourceService.testConnection(dataSource);
                if (test.getLeft().getLeft() && test.getRight().getLeft()) {
                    int add = dataSourceService.add(dataSource, true);
                    if (add != 0) {
                        result = Pair.of(1, "datasource exists");
                    }
                } else {
                    result = Pair.of(1, (test.getLeft().getLeft() ? "" : "主库: " + test.getLeft().getRight() + '\n')
                            + (test.getRight().getLeft() ? "" : "备库: " + test.getRight().getRight()));
                }
            } else {
                result = Pair.of(1, "copy datasource not found");
            }

            return result;
        } catch (SQLException | InterruptedException e) {
            return Pair.of(1, e.getMessage());
        }
    }

    private DataSource copyDataSource(DataSource dataSource, String dsName, SchemaApply schemaApply) {
        DataSourceEncryptUtils.decryptPassword(dataSource);
        dataSource.setEnv(schemaApply.getEnv());
        dataSource.setName(StringUtils.isNotBlank(dsName) ? dsName : schemaApply.getSid());
        dataSource.setSid(schemaApply.getSid());
        dataSource.setSid2(schemaApply.getSid());
        dataSource.setId(null);
        dataSource.setGmtModified(null);

        // proxy
        dataSource.setProxySid(schemaApply.getSid());

        return dataSource;
    }

    @Override
    protected Object findEntityByTask(int taskId) {
        return schemaApplyDao.findByTask(taskId);
    }

    @Override
    protected Object findEntityByTask(int taskId, String key) {
        return findEntityByTask(taskId);
    }

    @Override
    protected String getProcessDefinitionKey() {
        return PROCESS_DEFINITION_KEY;
    }
}
