package com.autodb.ops.dms.service.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.autodb.ops.dms.Main;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Task Test
 *
 * @author dongjs
 * @since 16/1/13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest(randomPort = true)
public class ActivitiTest {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FormService formService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testIdentity() throws Exception {
        Group admin = identityService.createGroupQuery().groupId("admin").singleResult();
        System.out.println(objectMapper.writeValueAsString(admin));

        User user = identityService.createUserQuery().userId("dongjs").singleResult();
        System.out.println(objectMapper.writeValueAsString(user));

        List<User> admins = identityService.createUserQuery().memberOfGroup("admin").list();
        System.out.println(objectMapper.writeValueAsString(admins));
    }

    @Test
    public void testStart() {
        TaskQuery taskQuery = taskService.createTaskQuery();

        System.out.println("Before start, number of tasks : " + taskQuery.count());

        Map<String, Object> variables = new HashMap<>();
        variables.put("applyUser", "dongjs");
        variables.put("dsList", Arrays.asList("dms", "common"));
        variables.put("reason", "reason...");
        runtimeService.startProcessInstanceByKey("ds-apply", variables);

        System.out.println("After start, number of tasks : " + taskQuery.count());
    }

    @Test
    public void testProcessInstanceQuery() {
        List<ProcessInstance> processes = runtimeService.createProcessInstanceQuery().includeProcessVariables().list();
        System.out.println("Number of Processes : " + processes.size());
        processes.forEach(process -> {
            System.out.println("Process: " + process.getId() + '-' + process.getProcessDefinitionKey());
            System.out.println("process variables: " + process.getProcessVariables());
        });
        System.out.println();

        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
                .includeProcessVariables()
                .excludeSubprocesses(false)
                .list();
        System.out.println("Number of Processes history: " + historicProcessInstances.size());
        historicProcessInstances.forEach(process -> {
            System.out.println("Process: " + process.getId());
            System.out.println("process start time: " + process.getStartTime());
            System.out.println("process variables: " + process.getProcessVariables());
        });
    }

    @Test
    public void testUserProcessInstanceQuery() {
        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
                //.startedBy("dongjs")
                //.involvedUser("dongjs")
                .variableValueEquals("ds", "wac_tongdun1")
                .finished()
                .includeProcessVariables()
                .excludeSubprocesses(false)
                .list();
        System.out.println("Number of Processes history: " + historicProcessInstances.size());
        historicProcessInstances.forEach(process -> {
            List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(process.getId())
                    .list().stream().filter(activity -> activity.getAssignee() != null).collect(Collectors.toList());

            System.out.println("Process: " + process.getId());
            System.out.println("process start time: " + process.getStartTime());
            System.out.println("process variables: " + process.getProcessVariables());
            System.out.println("process activities: " + activities);
            activities.forEach(activity -> {
                List<Comment> comments = taskService.getTaskComments(activity.getTaskId());
            });
        });
    }

    @Test
    public void testTaskQuery() {
        TaskQuery taskQuery = taskService.createTaskQuery();
        System.out.println("Number of tasks : " + taskQuery.count());
        System.out.println("Number of tasks unassigned : " + taskQuery.taskUnassigned().count());
        System.out.println();

        List<Task> tasks = taskQuery
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .list();
        printTask(tasks);
    }

    @Test
    public void testUserTaskQuery() {
        String userId = "dongjs";
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .list();
        printTask(tasks);
    }

    private void printTask(List<Task> tasks) {
        tasks.forEach(task -> {
            System.out.println("Task: " + task.getId() + '-' + task.getName());
            System.out.println("parent: " + task.getParentTaskId());
            System.out.println("category: " + task.getCategory());
            System.out.println("assignee: " + task.getAssignee());
            System.out.println("owner: " + task.getOwner());
            System.out.println("process variables: " + task.getProcessVariables());
            System.out.println("task variables: " + task.getTaskLocalVariables());
            System.out.println("task form: " + formService.getTaskFormData(task.getId()).getFormProperties());
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .includeProcessVariables()
                    .singleResult();

            System.out.println("Process Instance: " + processInstance.getId() + '-' + processInstance.getName());

            System.out.println(runtimeService.getVariables(task.getExecutionId()));

            System.out.println();
        });
    }
}
