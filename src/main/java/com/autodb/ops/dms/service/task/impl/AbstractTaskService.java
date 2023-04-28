package com.autodb.ops.dms.service.task.impl;

import com.autodb.ops.dms.common.DmsWebContext;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.dto.task.ActivityData;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.task.RuJob;
import com.autodb.ops.dms.entity.task.StructChange;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.task.RuJobDao;
import com.autodb.ops.dms.repository.task.TaskBizDao;
import com.autodb.ops.dms.repository.user.UserDao;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract TaskService
 *
 * @author dongjs
 * @since 16/1/24
 */
@Service
public abstract class AbstractTaskService {
    private static Logger log = LoggerFactory.getLogger(AbstractTaskService.class);

    @Value("${data.file.path}")
    protected String filePath;

    @Autowired
    protected TaskBizDao taskBizDao;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected IdentityService identityService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected RuJobDao ruJobDao;

    public int completeTask(User user, String taskDefinitionKey, String taskId) throws AppException {
        try {
            int result = 0;
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(getProcessDefinitionKey())
                    .taskDefinitionKey(taskDefinitionKey)
                    .taskId(taskId)
                    .singleResult();
            if (task != null) {
                taskService.claim(taskId, user.getUsername());
                taskService.complete(taskId);

            } else {
                result = 1;
            }

            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("{} completeTask {} id {} error", getProcessDefinitionKey(), taskDefinitionKey, taskId);
            return 1;
        }
    }

    public TaskData task(String definitionKey, String id) throws AppException {
        try {
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(getProcessDefinitionKey())
                    .taskDefinitionKey(definitionKey)
                    .taskId(id)
                    .singleResult();
            return task != null ? of(task) : null;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }



    public ProcessData process(String id) throws AppException {
        try {
            HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                    .processDefinitionKey(getProcessDefinitionKey())
                    .processInstanceId(id)
                    .singleResult();

            return processInstance != null ? of(processInstance) : null;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    public ProcessData processByTaskId(String definitionKey, String id) throws AppException {
        try {
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(getProcessDefinitionKey())
                    .taskDefinitionKey(definitionKey)
                    .taskId(id)
                    .singleResult();
            return task != null ? process(task.getProcessInstanceId()) : null;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    @Transactional
    public void end(String processInstanceId) throws AppException {
        TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(processInstanceId);
        if (taskBiz != null) {
            taskBiz.setStatus(TaskBiz.Status.END);
            taskBiz.setEndTime(new Date());
            taskBizDao.update(taskBiz);
        }
    }

    private TaskData of(Task task) throws AppException {
        TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
        if (null == taskBiz) {
            return null;
        }

        Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
        Object ds = variables.get("ds");
        if(ds.toString().split(";").length > 1){
            ds = ds.toString().split(";")[1];//cobar的用shard name代替
//            variables.put("ds",ds);
//            runtimeService.setVariables(task.getExecutionId(),variables);//更新到runtimeService里面
        }
        Object executeTime = variables.get("executeTime");
        if (ds != null) {
            taskBiz.setEntity(findEntityByTask(taskBiz.getId(), ds.toString()));
        } else {
            taskBiz.setEntity(findEntityByTask(taskBiz.getId()));
        }

        TaskData data = new TaskData();
        data.setTaskId(task.getId());
        data.setTaskKey(task.getTaskDefinitionKey());
        data.setTaskName(task.getName());
        data.setTaskTime(task.getCreateTime());
        if (executeTime != null && executeTime instanceof Date) {
            data.setExecuteTime((Date) executeTime);
        }else if(executeTime instanceof String){
            DateTime dateTime = new DateTime(executeTime);
            data.setExecuteTime(dateTime.toDate());
        }
        data.setTaskBiz(taskBiz);
        return data;
    }

    private ProcessData of(HistoricProcessInstance processInstance) throws AppException {
        TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(processInstance.getId());
        if (null == taskBiz) {
            return null;
        }
        ProcessData processData = new ProcessData();
        processData.setTaskBiz(taskBiz);
        taskBiz.setEntity(findEntityByTask(taskBiz.getId()));

        List<ActivityData> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getId())
                .list().stream()
                .filter(activity -> activity.getAssignee() != null
                        || "startevent".equalsIgnoreCase(activity.getActivityId())
                        || "endevent".equalsIgnoreCase(activity.getActivityId()))
                .map(activity -> {
                    ActivityData data = new ActivityData();
                    data.setName(activity.getActivityName());
                    String assignee = activity.getAssignee();
                    if(!Strings.isNullOrEmpty(assignee)){
                        User user = userDao.findByUsername(assignee);
                        if(user == null)//fixme why null
                            data.setAssignee(assignee);
                        else
                            data.setAssignee(user.getNickname());
                    }else{
                        data.setAssignee(activity.getAssignee());
                    }
                    data.setTime(activity.getEndTime() != null ? activity.getTime() : activity.getStartTime());

                    List<Comment> comments = taskService.getTaskComments(activity.getTaskId());
                    if (comments != null && comments.size() > 0) {
                        data.setComment(comments.get(0).getFullMessage());
                    }
                    return data;
                }).sorted(Comparator.comparing(ActivityData::getTime))
                .collect(Collectors.toList());
        processData.setActivities(activities);

        List<String> currUsers = Lists.newArrayList();
        if (TaskBiz.Status.PROCESS.equals(taskBiz.getStatus())) {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
            if (tasks.size() == 1) {
                List<String> currUserNames = Collections.emptyList();
                currUserNames = taskService.getIdentityLinksForTask(tasks.get(0).getId()).stream()
                        .map(IdentityLink::getUserId)
                        .collect(Collectors.toList());
                List<User> users = userDao.findByUsernames(currUserNames);
                for(User user : users){
                    currUsers.add(user.getNickname());
                }
            }
            if (taskBiz.getStartUser().getUsername().equals(DmsWebContext.get().getUsername())) {
                processData.setCanCancel(true);
            }
        }
        processData.setCurrUsers(currUsers);

        return processData;
    }

    protected abstract Object findEntityByTask(int taskId);
    protected abstract Object findEntityByTask(int taskId, String key);
    protected abstract String getProcessDefinitionKey();

    protected Pair<String, String> getExportFileName() throws IOException {
        return getFileName("export", ".csv");
    }

    protected Pair<String, String> getBackupFileName() throws IOException {
        return getFileName("backup", ".csv");
    }

    protected Pair<String, String> getSqlFileName() throws IOException {
        return getFileName("sql", ".sql");
    }

    protected Pair<String, String> getFileName(String module, String suffix) throws IOException {
        String absolutePath = filePath.endsWith(File.separator) ? filePath : filePath + File.separator;
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        StringBuilder fileString = new StringBuilder();
        fileString.append(absolutePath).append(module).append(File.separator)
                .append(c.get(Calendar.YEAR)).append('_').append(c.get(Calendar.MONTH) + 1);
        File dir = new File(fileString.toString());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("mkdirs failed");
        }

        String filename = fileString.append(File.separator)
                .append(c.get(Calendar.DATE)).append("_").append(c.get(Calendar.HOUR_OF_DAY))
                .append("_").append(c.get(Calendar.MINUTE)).append("_").append(c.get(Calendar.SECOND))
                .append("_").append(c.get(Calendar.MILLISECOND)).append(suffix).toString();

        return Pair.of(filename, filename.substring(absolutePath.length()));
    }


}
