package com.autodb.ops.dms.service.task.impl;

import com.google.common.base.Joiner;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.task.DsApply;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.task.DsApplyDao;
import com.autodb.ops.dms.repository.user.UserDao;
import com.autodb.ops.dms.service.datasource.DataSourceAuthService;
import com.autodb.ops.dms.service.task.DsApplyService;
import com.google.common.collect.Maps;
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
 * DsApplyService Impl
 *
 * @author dongjs
 * @since 16/1/14
 */
@Service
public class DsApplyServiceImpl extends AbstractTaskService implements DsApplyService {
    private static Logger log = LoggerFactory.getLogger(DsApplyServiceImpl.class);

    private static final String PROCESS_DEFINITION_KEY = "ds-apply";

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DsApplyDao dsApplyDao;

    @Autowired
    private DataSourceAuthService dataSourceAuthService;

    @Override
    @Transactional
    public int apply(User user, String env, List<Integer> dsList, String reason) throws AppException {
        try {
            Map<Integer, DataSource> dataSourceMap = dataSourceDao.findUnAuthByUserEnv(user.getId(), env).stream()
                    .collect(Collectors.toMap(DataSource::getId, dataSource -> dataSource));

            List<DataSource> dataSourceList = dsList.stream()
                    .map(dataSourceMap::get).filter(ds -> ds != null)
                    .collect(Collectors.toList());

            if (dataSourceList.size() < 1) {
                return 1;
            }

            List<Integer> dsIdList = dataSourceList.stream().map(DataSource::getId).collect(Collectors.toList());
            List<String> dsNameList = dataSourceList.stream().map(DataSource::getName).collect(Collectors.toList());

            // task biz
            TaskBiz taskBiz = new TaskBiz();
            taskBiz.setType(TaskBiz.Type.DS_APPLY);
            taskBiz.setStartUser(user);
            taskBiz.setStatus(TaskBiz.Status.PROCESS);
            taskBiz.setStartTime(new Date());
            taskBiz.setInfo(Joiner.on(',').join(dsNameList));
            taskBiz.setExplain(reason);
            taskBizDao.add(taskBiz);

            Map<String,String> dsName = Maps.newHashMap();
            Map<String,String> dsEnvs = Maps.newHashMap();

            dataSourceList.forEach(ds -> {
                dsName.put(ds.getId()+"",ds.getName());
                dsEnvs.put(ds.getId()+"",ds.getEnv());
                DsApply dsApply = new DsApply();
                dsApply.setTask(taskBiz);
                dsApply.setKey(ds.getId().toString());
                dsApply.setReason(reason);
                dsApply.setDsEnv(ds.getEnv());
                dsApply.setDsName(ds.getName());
                dsApplyDao.add(dsApply);
            });

            Map<String, Object> variables = new HashMap<>();
            variables.put("applyUser", user.getUsername());
            variables.put("dsList", dsIdList);
            variables.put("dsName", dsName);
            variables.put("dsEnvs", dsEnvs);
            variables.put("dsEnv", env);
            identityService.setAuthenticatedUserId(user.getUsername());
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                    taskBiz.getId().toString(), variables);

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
    public int approve(String id, User assessor, boolean agree, String reason, List<String> roles) throws AppException {
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
                String applyUser = variables.get("applyUser").toString();
                TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                DsApply dsApply = dsApplyDao.findByTask(taskBiz.getId(), ds);

                dsApply.setAssessor(assessor.getNickname());
                dsApply.setAssessType(agree ? DsApply.AssessType.AGREE : DsApply.AssessType.REJECT);
                dsApply.setAssessTime(new Date());
                dsApply.setAssessRemark(reason);
                dsApplyDao.update(dsApply);

                if (agree) {
                    // ds auth role
                    DataSource dataSource = dataSourceDao.find(ds);
                    User user = userDao.findByUsername(applyUser);
                    if (dataSource != null && user != null) {
                        dataSourceAuthService.add(dataSource, user, roles);
                    }
                }

                taskService.claim(id, assessor.getUsername());
                taskService.addComment(id, task.getProcessInstanceId(), reason);

                Map<String, Object> vars = new HashMap<>();
                vars.put("assessorUser",assessor.getUsername());
                vars.put("approved", String.valueOf(agree));
                vars.put("comment", reason);
                taskService.complete(id, vars);
            } else {
                result = 1;
            }

            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("ds-apply approve error", e);
            return 1;
        }
    }

    @Override
    @Transactional
    public int adjust(User user, String id, boolean reApply, String reason) throws AppException {
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
                    DsApply dsApply = dsApplyDao.findByTask(taskBiz.getId(), ds);

                    dsApply.setReason(reason);
                    dsApplyDao.update(dsApply);
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
            log.warn("ds-apply adjust error", e);
            return 1;
        }
    }

    @Override
    protected Object findEntityByTask(int taskId) {
        return dsApplyDao.findByTask(taskId);
    }

    @Override
    protected Object findEntityByTask(int taskId, String key) {
        return dsApplyDao.findByTask(taskId, key);
    }

    @Override
    protected String getProcessDefinitionKey() {
        return PROCESS_DEFINITION_KEY;
    }
}
