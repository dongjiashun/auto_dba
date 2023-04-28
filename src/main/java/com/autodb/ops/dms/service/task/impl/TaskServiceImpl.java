package com.autodb.ops.dms.service.task.impl;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.data.pagination.Pagination;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.common.util.StringUtil;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.ProcessDataQuery;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.task.RuTask;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.task.RuTaskDao;
import com.autodb.ops.dms.repository.task.TaskBizDao;
import com.autodb.ops.dms.service.task.TaskService;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskService Impl
 *
 * @author dongjs
 * @since 16/1/15
 */
@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    protected IdentityService identityService;

    @Autowired
    private org.activiti.engine.TaskService taskService;

    @Autowired
    private TaskBizDao taskBizDao;

    @Autowired
    private RuTaskDao ruTaskDao;

    @Override
    public List<TaskData> userTasks(User user) throws AppException {
        try {
            List<Task> tasks = taskService.createTaskQuery()
                    .taskCandidateOrAssigned(user.getUsername())
                    .orderByTaskCreateTime().desc()
                    .list();

            return ofTaskData(tasks);
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    @Override
    public int userTasksCount(User user) throws AppException {
        try {
            return (int) taskService.createTaskQuery()
                    .taskCandidateOrAssigned(user.getUsername())
                    .count();
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    @Override
    public List<ProcessData> userProcesses(User user, Page<ProcessData> page) throws AppException {
        try {
            Pagination pagination = page.pagination;

            long count = historyService.createHistoricProcessInstanceQuery()
                    .startedBy(user.getUsername()).count();
            pagination.setRowCount((int) count);

            List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
                    .startedBy(user.getUsername())
                    .orderByProcessInstanceStartTime().desc()
                    .listPage(pagination.getOffset(), pagination.getLimit());

            List<ProcessData> processData = ofProcessData(processInstances);
            page.setData(processData);

            return processData;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    @Override
    public List<ProcessData> userRelatedProcesses(User user, ProcessDataQuery processDataQuery,
                                                  Page<ProcessData> page) throws AppException {
        try {
            Pagination pagination = page.pagination;

            long count = this.buildHistoricProcessInstanceQuery(processDataQuery)
                    .involvedUser(user.getUsername()).count();
            pagination.setRowCount((int) count);

            List<HistoricProcessInstance> processInstances = this.buildHistoricProcessInstanceQuery(processDataQuery)
                    .involvedUser(user.getUsername())
                    .orderByProcessInstanceStartTime().desc()
                    .listPage(pagination.getOffset(), pagination.getLimit());

            List<ProcessData> processData = ofProcessData(processInstances);
            page.setData(processData);

            return processData;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    @Override
    public List<ProcessData> allProcesses(ProcessDataQuery processDataQuery, Page<ProcessData> page) throws AppException {
        try {
            Pagination pagination = page.pagination;

            if(!Strings.isNullOrEmpty(processDataQuery.getTaskState())){
                List<RuTask> ruTasks = ruTaskDao.findByTaskDefKey(processDataQuery.getTaskState());
                if(ruTasks.size() <= 0){
                    page.setData(Lists.newArrayList());
                    return Lists.newArrayList();
                }
                pagination.setRowCount((int) ruTasks.size());
                Set<String> processInstanceIds = Sets.newHashSet();
                for(RuTask ruTask : ruTasks){
                    processInstanceIds.add(ruTask.getProcess_instance_id());
                }
                List<HistoricProcessInstance> processInstances = this.buildHistoricProcessInstanceQuery(processDataQuery)
                        .processInstanceIds(processInstanceIds)
                        .orderByProcessInstanceStartTime().desc()
                        .listPage(pagination.getOffset(), pagination.getLimit());

                List<ProcessData> processData = ofProcessData(processInstances);
                page.setData(processData);
                return processData;
            }else{
                long count = this.buildHistoricProcessInstanceQuery(processDataQuery).count();
                pagination.setRowCount((int) count);

                List<HistoricProcessInstance> processInstances = this.buildHistoricProcessInstanceQuery(processDataQuery)
                        .orderByProcessInstanceStartTime().desc()
                        .listPage(pagination.getOffset(), pagination.getLimit());

                List<ProcessData> processData = ofProcessData(processInstances);
                page.setData(processData);
                return processData;
            }
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    @Override
    @Transactional
    public int cancel(String startUser, String processInstanceId) throws AppException {
        try {
            int code = 0;

            TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(processInstanceId);
            if (taskBiz != null
                    && taskBiz.getStartUser().getUsername().equals(startUser)
                    && TaskBiz.Status.PROCESS.equals(taskBiz.getStatus())
                    && null != runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult()) {
                // runtimeService.suspendProcessInstanceById(processInstanceId);
                runtimeService.deleteProcessInstance(processInstanceId, TaskBiz.Status.CANCEL);

                taskBiz.setStatus(TaskBiz.Status.CANCEL);
                taskBiz.setEndTime(new Date());
                taskBizDao.update(taskBiz);
            } else {
                code = 1;
            }

            return code;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    public HistoricProcessInstanceQuery buildHistoricProcessInstanceQuery(ProcessDataQuery query) {
        HistoricProcessInstanceQuery processQuery = historyService.createHistoricProcessInstanceQuery();
        if (StringUtils.isNoneEmpty(query.getProcess())) {
            processQuery.processDefinitionKey(query.getProcess());
        }
        if (query.getFrom() != null) {
            processQuery.startedAfter(query.getFrom());
        }
        if (query.getTo() != null) {
            processQuery.startedBefore(query.getTo());
        }
        if (query.getDatasource() != null && query.getDatasource() > 0) {
            processQuery.variableValueEquals("ds", query.getDatasource());
        }
        if (StringUtils.isNoneEmpty(query.getEnv())) {
            processQuery.variableValueEquals("dsEnv", DataSource.Env.getEnv(query.getEnv()));
        }

        if (query.getFinished() >= 0) {
            if (query.getFinished() == 0) {
                processQuery.unfinished();
            } else {
                processQuery.finished();
            }
        }
        if (StringUtils.isNoneEmpty(query.getUser())) {
            processQuery.startedBy(query.getUser());
        }

        return processQuery;
    }

    private List<TaskData> ofTaskData(List<Task> tasks) throws AppException {
        Map<String, TaskBiz> taskBizMap = taskBizDao.findByProcessInstanceIds(tasks.stream()
                .map(Task::getProcessInstanceId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(TaskBiz::getProcessInstanceId, task -> task));


        return tasks.stream().map(task -> {
            Map<String, Object> variables = taskService.getVariables(task.getId(), Arrays.asList("dsEnv", "dsName","ds"));
            TaskBiz taskBiz = taskBizMap.get(task.getProcessInstanceId());

            TaskData data = new TaskData();
            data.setTaskId(task.getId());
            data.setTaskKey(task.getTaskDefinitionKey());
            data.setTaskName(task.getName());
            data.setTaskTime(task.getCreateTime());



            Map<String, Object> runtimeVariables = runtimeService.getVariables(task.getExecutionId());
            String realName = taskBiz.getInfo();
            String dsEnv = "prod";
            if(variables.containsKey("dsEnv"))
                dsEnv = variables.get("dsEnv").toString();

            String ds = "";
            if(runtimeVariables.containsKey("ds")){
                ds = runtimeVariables.get("ds").toString();
                if(!runtimeVariables.containsKey(task.getProcessInstanceId())){
                    runtimeVariables.put(task.getProcessInstanceId(),ds);
                    runtimeService.setVariables(task.getExecutionId(),runtimeVariables);
                }
            }else if(runtimeVariables.containsKey(task.getProcessInstanceId())){
                ds = runtimeVariables.get(task.getProcessInstanceId()).toString();
            }

            if(runtimeVariables.containsKey("dsName")){
                Map<String,String> dsName = (Map)runtimeVariables.get("dsName");
                if(!Strings.isNullOrEmpty(ds))
                    realName = dsName.get(ds);
            }
            if(runtimeVariables.containsKey("dsEnvs")){
                Map<String,String> dsEnvs = (Map)runtimeVariables.get("dsEnvs");
                if(!Strings.isNullOrEmpty(ds))
                    dsEnv = dsEnvs.get(ds);
            }

            data.setTaskDsName(realName);
            data.setTaskDsEnv(DataSource.Env.getEnv(dsEnv));
            data.setTaskBiz(taskBiz);
            return data;
        }).filter(task -> task.getTaskBiz() != null).collect(Collectors.toList());
    }

    private List<ProcessData> ofProcessData(List<HistoricProcessInstance> processInstances) throws AppException {
        Map<String, TaskBiz> taskBizMap = taskBizDao.findByProcessInstanceIds(processInstances.stream()
                .map(HistoricProcessInstance::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(TaskBiz::getProcessInstanceId, task -> task));

        return processInstances.stream().map(process -> {
            ProcessData data = new ProcessData();

            TaskBiz taskBiz = taskBizMap.get(process.getId());
            if (null != taskBiz) {
                if (taskBiz.getEndTime() == null) {
                    List<Task> tasks = taskService.createTaskQuery().processInstanceId(process.getId()).active().list();

                    if (null != tasks && tasks.size() > 0) {
                        data.setActiveTask(tasks.get(0).getName());
                    }
                }
                data.setTaskBiz(taskBiz);
            }
            return data;
        }).filter(process -> process.getTaskBiz() != null).collect(Collectors.toList());
    }

    private List<ProcessData> ofProcessData(List<HistoricProcessInstance> processInstances,String taskDefinitionKey) throws AppException {
        Map<String, TaskBiz> taskBizMap = taskBizDao.findByProcessInstanceIds(processInstances.stream()
                .map(HistoricProcessInstance::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(TaskBiz::getProcessInstanceId, task -> task));

        return processInstances.stream().map(process -> {
            ProcessData data = new ProcessData();

            TaskBiz taskBiz = taskBizMap.get(process.getId());
            if (null != taskBiz) {
                if (taskBiz.getEndTime() == null) {
                    List<Task> tasks = Lists.newArrayList();
                    if(Strings.isNullOrEmpty(taskDefinitionKey)){
                        tasks = taskService.createTaskQuery().processInstanceId(process.getId()).active().list();
                    }else{
                        tasks = taskService.createTaskQuery().processInstanceId(process.getId()).taskDefinitionKey(taskDefinitionKey).active().list();
                    }

                    if (null != tasks && tasks.size() > 0) {
                        data.setActiveTask(tasks.get(0).getName());
                        data.setTaskBiz(taskBiz);//这个必须添加 否则不会有记录
                    }
                }

                if(Strings.isNullOrEmpty(taskDefinitionKey)){
                    data.setTaskBiz(taskBiz);
                }

            }
            return data;
        }).filter(process -> process.getTaskBiz() != null).collect(Collectors.toList());
    }
}
