package com.autodb.ops.dms.domain.activiti;

import com.google.common.collect.ImmutableMap;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.domain.dingding.DingdingService;
import com.autodb.ops.dms.domain.email.EmailDo;
import com.autodb.ops.dms.domain.email.EmailService;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.user.UserDao;
import com.google.common.collect.Lists;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Email Service
 *
 * @author dongjs
 * @since 16/1/20
 */
@Component
public class TaskEmailService {
    private static Logger log = LoggerFactory.getLogger(TaskEmailService.class);

    public static final String ROLE_ADMIN = "admin";
    public static final String EMAIL_SUFFIX = "@dianwoba.com";

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TaskDingdingService taskDingdingService;

    /**
     * 发送邮件给bi
     * @param processName 处理名称【新建表，删除表，增加列】
     * @param sql  具体的执行sql
     */
    public void notify_bi(String processName,String database,String sql){
        try {
            Map<String, Object> context = new HashMap<>();
            context.put("database", database);
            context.put("content", sql);
            context.put("processName", processName);

            EmailDo emailDo = new EmailDo();
            emailDo.setTemplate("mail/bi_notify.vm.html");
            List<String> to = Lists.newArrayList();
//            bi@dianwoba.com  qianfeng@dianwoba.com
            to.add("bi@dianwoba.com");
            emailDo.setTo(to);
            List<String> cc = Lists.newArrayList();
            cc.add("xieliuping@dianwoba.com");
            emailDo.setCc(cc);
            emailDo.setSubject("DMS通知");
            emailDo.setContext(context);
            emailService.sendMain(emailDo);
        } catch (Exception e) {
            log.warn("发送邮件给bi失败，",e);
        }
    }
    
    /**
     * dongjs
     * 邮箱通用方法
     */
    public void mailsend(String processName ,    // 流程类型名字 
    						String applyUser,    //  申请人
    						String host,         //  自动化平台url
    						String content,      //  内容
    						String comment,      //  审核备注
    						String executed,     //  执行状态
    						String mailtemplate, //  邮箱模板
    						String mail          //  邮箱地址
    						){
    	 try {
             Map<String, Object> context = new HashMap<>();
             context.put("processName", processName);
             context.put("applyUser", applyUser);
             context.put("host", host);
             context.put("content",content);
             context.put("comment",comment);
             context.put("executed",executed);
             EmailDo emailDo = new EmailDo();
//             "mail/bi_notify.vm.html"
             emailDo.setTemplate("mail/"+mailtemplate);
             List<String> to = Lists.newArrayList();
             to.add(mail);
             emailDo.setTo(to);
             List<String> cc = Lists.newArrayList();
             cc.add("dongjiashun@htrader.cn.com");
             emailDo.setCc(cc);
             emailDo.setSubject("DMS通知");
             emailDo.setContext(context);
             emailService.sendMain(emailDo);
         } catch (Exception e) {
             log.warn("发送邮件给bi失败，",e);
         }
    }
    /**
     * task apply<br/>
     * to: candidates,admin
     */
    public void taskApply(ExecutionEntity execution) throws AppException {
        try {
            Task task = getTask(execution);
            String processName = getProcessName(execution);
            String applyUser = execution.getVariable("applyUser").toString();
            String title = execution.getVariable("title") != null ? execution.getVariable("title").toString() : null;
            Pair<String, String> nameEnvPair = this.getNameEnvPair(execution);

            // hack
            execution.setVariablesLocal(ImmutableMap.of("dsName", nameEnvPair.getLeft(),
                    "dsEnv", nameEnvPair.getRight()));

            // mail to and cc
            Set<String> candidates = new HashSet<>();
            List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
            links.forEach(link -> {
                if (IdentityLinkType.CANDIDATE.equals(link.getType())) {
                    User user = userDao.findByUsername(link.getUserId());//taskuserId-->autodb.userCode-->userDao.userName
                    if(user != null && user.getEmail() != null)
                        candidates.add(user.getEmail());
                }
            });
            Set<String> admins = userDao.findByRole(ROLE_ADMIN).stream()
                    .map(User::getEmail).collect(Collectors.toSet());

            // subject
            StringBuilder subjectSb = new StringBuilder().append(DataSource.Env.getEnvName(nameEnvPair.getRight()))
                    .append('-').append(processName).append('-').append(nameEnvPair.getLeft());
            if (title != null) {
                subjectSb.append(':').append(title);
            }
            String subject = subjectSb.toString();

            // context
            Map<String, Object> context = new HashMap<>();
            context.put("content", subject);
            context.put("processName", processName);
            User user = userDao.findByUsername(applyUser);
            context.put("applyUser", user.getNickname());

            EmailDo emailDo = new EmailDo();
            emailDo.setTemplate("mail/task_start.vm.html");
            emailDo.setTo(new ArrayList<>(candidates));
            emailDo.setCc(new ArrayList<>(admins));
            emailDo.setSubject(subject);
            emailDo.setContext(context);
            emailService.sendMain(emailDo);
        } catch (ActivitiException | NullPointerException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    /**
     * task agree<br/>
     * to: starter
     */
    public void taskAgree(ExecutionEntity execution) throws AppException {
        try {
            Object approved = execution.getVariable("approved");
            if (approved == null
                    || "false".equalsIgnoreCase(approved.toString())
                    || "0".equalsIgnoreCase(approved.toString())) {
                return;
            }

            // agree
            String processName = getProcessName(execution);
            String applyUser = execution.getVariable("applyUser").toString();
            String comment = execution.getVariable("comment").toString();
            String title = execution.getVariable("title") != null ? execution.getVariable("title").toString() : null;
            Pair<String, String> nameEnvPair = this.getNameEnvPair(execution);

            // subject
            StringBuilder subjectSb = new StringBuilder().append(DataSource.Env.getEnvName(nameEnvPair.getRight()))
                    .append('-').append(processName).append('-').append(nameEnvPair.getLeft());
            if (title != null) {
                subjectSb.append(':').append(title);
            }
            String subject = subjectSb.toString();

            // context
            Map<String, Object> context = new HashMap<>();
            context.put("content", subject);
            context.put("processName", processName);
            context.put("applyUser", applyUser);
            context.put("comment", comment);

            EmailDo emailDo = new EmailDo();
            emailDo.setTemplate("mail/task_agree.vm.html");
            emailDo.setTo(Collections.singletonList(applyUser + EMAIL_SUFFIX));
            emailDo.setSubject(subject);
            emailDo.setContext(context);
            emailService.sendMain(emailDo);
        } catch (ActivitiException | NullPointerException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    /**
     * task adjust<br/>
     * to: assignee
     */
    public void taskAdjust(ExecutionEntity execution) throws AppException {
        try {
            String processName = getProcessName(execution);
            String applyUser = execution.getVariable("applyUser").toString();
            String comment = execution.getVariable("comment").toString();
            String title = execution.getVariable("title") != null ? execution.getVariable("title").toString() : null;
            Pair<String, String> nameEnvPair = this.getNameEnvPair(execution);

            // subject
            StringBuilder subjectSb = new StringBuilder().append(DataSource.Env.getEnvName(nameEnvPair.getRight()))
                    .append('-').append(processName).append('-').append(nameEnvPair.getLeft());
            if (title != null) {
                subjectSb.append(':').append(title);
            }
            String subject = subjectSb.toString();

            // context
            Map<String, Object> context = new HashMap<>();
            context.put("content", subject);
            context.put("processName", processName);
            context.put("applyUser", applyUser);
            context.put("comment", comment);

            EmailDo emailDo = new EmailDo();
            emailDo.setTemplate("mail/task_adjust.vm.html");
            emailDo.setTo(Collections.singletonList(applyUser + EMAIL_SUFFIX));
            emailDo.setSubject(subject);
            emailDo.setContext(context);
            emailService.sendMain(emailDo);
        } catch (ActivitiException | NullPointerException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    /**
     * task execute<br/>
     * to: assignee
     */
    public void taskExecute(ExecutionEntity execution) throws AppException {
        try {
            Object executed = execution.getVariable("executed");
            if (executed == null) {
                return;
            }

            String processName = getProcessName(execution);
            String applyUser = execution.getVariable("applyUser").toString();
            String comment = execution.getVariable("comment").toString();
            String title = execution.getVariable("title") != null ? execution.getVariable("title").toString() : null;
            Pair<String, String> nameEnvPair = this.getNameEnvPair(execution);

            // subject
            StringBuilder subjectSb = new StringBuilder().append(DataSource.Env.getEnvName(nameEnvPair.getRight()))
                    .append('-').append(processName).append('-').append(nameEnvPair.getLeft());
            if (title != null) {
                subjectSb.append(':').append(title);
            }
            String subject = subjectSb.toString();

            // context
            Map<String, Object> context = new HashMap<>();
            context.put("content", subject);
            context.put("processName", processName);
            context.put("applyUser", applyUser);
            context.put("executed", executed);
            context.put("comment", comment);

            EmailDo emailDo = new EmailDo();
            emailDo.setTemplate("mail/task_execute.vm.html");
            emailDo.setTo(Collections.singletonList(applyUser + EMAIL_SUFFIX));
            emailDo.setSubject(subject);
            emailDo.setContext(context);
            emailService.sendMain(emailDo);
        } catch (ActivitiException | NullPointerException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    private Task getTask(ExecutionEntity execution) throws AppException {
        List<TaskEntity> tasks = execution.getTasks();
        if (tasks == null || tasks.size() == 0) {
            throw new AppException(ExCode.WF_001, "error task listener");
        }

        return tasks.get(0);
    }

    private Pair<String, String> getNameEnvPair(ExecutionEntity execution) {
        String ds = execution.getVariable("ds").toString();
        DataSource dataSource = dataSourceDao.find(ds);

        return dataSource != null ? Pair.of(dataSource.getName(), dataSource.getEnv())
                : Pair.of(ds, execution.getVariable("dsEnv").toString());
    }

    private String getProcessName(ExecutionEntity execution) {
        String key = execution.getProcessDefinition().getKey();
        String name;
        switch (key) {
            case "ds-apply":
                name = "数据源申请";
                break;
            case "data-export":
                name = "数据导出";
                break;
            case "data-change":
                name = "数据变更";
                break;
            case "struct-change":
                name = "结构变更";
                break;
            case "schema-apply":
                name = "创建数据源申请";
                break;
            case "canal-apply":
                name = "创建Canal同步申请";
                break;
            default:
                name = key;
        }

        return name;
    }
}