package com.autodb.ops.dms.service.task.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.domain.canal.CanalService;
import com.autodb.ops.dms.dto.task.CanalApplyAuditForm;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceCobar;
import com.autodb.ops.dms.entity.task.CanalApply;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceCobarDao;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.task.CanalApplyDao;
import com.autodb.ops.dms.service.task.CanalApplyService;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CanalApplyService Impl
 *
 * @author dongjs
 * @since 2016/11/1
 */
@Service
public class CanalApplyServiceImpl extends AbstractTaskService implements CanalApplyService {
    private static Logger log = LoggerFactory.getLogger(CanalApplyServiceImpl.class);

    private static final String PROCESS_DEFINITION_KEY = "canal-apply";

    @Autowired
    private CanalApplyDao canalApplyDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private CanalService canalService;

    @Autowired
    private DataSourceCobarDao dataSourceCobarDao;

    @Override
    @Transactional
    public int apply(User user, String env, Integer dsId, String table, String reason) throws AppException {
        try {
            DataSource dataSource = dataSourceDao.find(dsId);
            if (dataSource == null) {
                return 1;
            }

            String sid = dataSource.getSid();

            // task biz
            TaskBiz taskBiz = new TaskBiz();
            taskBiz.setType(TaskBiz.Type.CANAL_APPLY);
            taskBiz.setStartUser(user);
            taskBiz.setStatus(TaskBiz.Status.PROCESS);
            taskBiz.setStartTime(new Date());
            taskBiz.setInfo(sid);
            taskBiz.setExplain(reason);
            taskBizDao.add(taskBiz);

            CanalApply canalApply = new CanalApply();
            canalApply.setTask(taskBiz);
            canalApply.setEnv(env);
            canalApply.setSid(sid);
            canalApply.setTable(table);
            canalApply.setReason(reason);
            canalApplyDao.add(canalApply);

            identityService.setAuthenticatedUserId(user.getUsername());
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                    taskBiz.getId().toString(),
                    ImmutableMap.of("applyUser", user.getUsername(), "ds", dataSource.getId()));

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
    public Pair<Integer, String> approve(String id, User assessor, CanalApplyAuditForm auditForm) throws AppException {
        try {
            Pair<Integer, String> result = Pair.of(0, null);
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(PROCESS_DEFINITION_KEY)
                    .taskDefinitionKey("audit")
                    .taskId(id)
                    .singleResult();

            if (task != null) {
                Byte agree = auditForm.getAgree();
                String reason = auditForm.getReason();

                TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                CanalApply canalApply = canalApplyDao.findByTask(taskBiz.getId());

                // 自动创建
                Pair<Integer, String> addRet = Pair.of(0, null);
                if (CanalApply.AssessType.AGREE == agree) {
                    addRet = addSync(canalApply, auditForm);
                }
                if (addRet.getLeft() == 0) {
                    auditForm.applyTo(canalApply);
                    canalApply.setAssessor(assessor.getUsername());
                    canalApply.setAssessType(agree);
                    canalApply.setAssessTime(new Date());
                    canalApply.setAssessRemark(reason);
                    canalApplyDao.update(canalApply);

                    taskService.claim(id, assessor.getUsername());
                    taskService.addComment(id, task.getProcessInstanceId(), reason);

                    Map<String, Object> vars = new HashMap<>();
                    vars.put("approved", CanalApply.AssessType.REJECT == agree ? "false" : "true");
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
            log.warn("canal-apply approve error", e);
            return Pair.of(1, null);
        }
    }

    @Override
    @Transactional
    public int adjust(User user, String id, Boolean reApply, String reason) throws AppException {
        try {
            int result = 0;
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
                    CanalApply canalApply = canalApplyDao.findByTask(taskBiz.getId());

                    canalApply.setReason(reason);
                    canalApplyDao.update(canalApply);
                }

                taskService.claim(id, user.getUsername());
                taskService.addComment(id, task.getProcessInstanceId(), reason);
                taskService.complete(id, Collections.singletonMap("reApply", String.valueOf(reApply)));
            } else {
                result = 1;
            }

            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("canal-apply adjust error", e);
            return 1;
        }
    }

    private Pair<Integer, String> addSync(CanalApply canalApply, CanalApplyAuditForm auditForm) {
        Pair<Integer, String> result = Pair.of(0, null);
        DataSource ds = dataSourceDao.findByEnvSid(canalApply.getEnv(), canalApply.getSid());
        if (ds != null) {
            try {
                if (!ds.isCobar()) {
                    CanalService.Result<String> sync = canalService.addSync(ds.getHost2(), ds.getPort2(), ds.getSid(),
                            canalApply.getTable(), auditForm.getTarget(), auditForm.getIndex(), auditForm.getKey(),
                            auditForm.getManager());
                    if (sync.getCode() != 0) {
                        result = Pair.of(1, sync.getError());
                    }
                } else {
                    DataSourceCobar cobar = dataSourceCobarDao.findByDataSource(ds);
                    if (cobar != null) {
                        List<String> errors = cobar.shardings()
                                .stream()
                                .map(sharding -> canalService.addSync(sharding.getSlaveHost(), sharding.getSlavePort(),
                                        ds.getSid(), canalApply.getTable(), auditForm.getTarget(),
                                        auditForm.getIndex(), auditForm.getKey(), auditForm.getManager()))
                                .filter(sync -> sync.getCode() != 0)
                                .map(CanalService.Result::getError)
                                .collect(Collectors.toList());

                        if (!errors.isEmpty()) {
                            result = Pair.of(1, Joiner.on("\n").join(errors));
                        }
                    } else {
                        result = Pair.of(1, "cobar metadata not found");
                    }
                }
            } catch (Exception e) {
                log.error("canal service call failed", e);
                result = Pair.of(1, "canal service call failed");
            }
        } else {
            result = Pair.of(1, "datasource not exists");
        }
        return result;
    }

    @Override
    public List<CanalService.Manager> managers(TaskBiz taskBiz) throws AppException {
        List<CanalService.Manager> result = Collections.emptyList();
        if (taskBiz.getEntity() instanceof CanalApply) {
            CanalApply canalApply = (CanalApply) taskBiz.getEntity();
            DataSource dataSource = dataSourceDao.findByEnvSid(canalApply.getEnv(), canalApply.getSid());
            if (dataSource != null) {
                try {
                    CanalService.Result<List<CanalService.Manager>> manager = canalService.manager(
                            dataSource.getHost2(), dataSource.getPort2());
                    if (manager.getData() != null) {
                        result = manager.getData();
                    }
                } catch (Exception e) {
                    log.info("canal service call failed", e);
                }
            }
        }

        return result;
    }

    @Override
    protected Object findEntityByTask(int taskId) {
        return canalApplyDao.findByTask(taskId);
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
