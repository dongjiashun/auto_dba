package com.autodb.ops.dms.domain.activiti;

import com.autodb.ops.dms.entity.task.StructChange;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.repository.task.TaskBizDao;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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
import com.google.common.collect.Maps;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TaskDingdingService {
    private static Logger log = LoggerFactory.getLogger(TaskDingdingService.class);

    @Value("${server.host}")
    private String serverHost;

    @Value("${api.dingdev.token}")
    private String devToken;

    @Value("${api.dingdba.token}")
    private String dbaToken;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSourceDao dataSourceDao;


    @Autowired
    private DingdingService dingdingService;

    @Autowired
    protected TaskBizDao taskBizDao;
    @Autowired
    private EmailService emailService;
    @Autowired
    CandidateUsersService candidateUsersService;

    private Map<String,Integer> processInstanceSize = Maps.newHashMap();
    private Map<String,Integer> processInstanceOccur = Maps.newHashMap();

    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void combineMessageAndSend(String token,String  userName,String executeUerName,List moblieList,String title,String executeResult,String rejectReason,String env,String processName) throws JsonProcessingException {
        if((env.equals("test") && (processName.equals("数据源申请")|| processName.equals("数据导出") || processName.equals("数据变更"))) || env.equals("dev"))//如果是测试环境的申请，不发送钉钉@
            return;
        HashMap contentMap = new HashMap();
        String strContent = "\n申请人：" + userName + "\n审核人：" + executeUerName + "\n工单地址：" + serverHost +"\n执行结果：" +executeResult + "\n审核建议："+rejectReason;
        String messageContent = title+strContent;
        contentMap.put("msgtype","text");
        contentMap.put("text",ImmutableMap.of("content",messageContent));
        contentMap.put("at",ImmutableMap.of("atMobiles",moblieList,"isAtAll","false"));

        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(contentMap);
        } catch (JsonProcessingException e) {
            throw e;
        }
        try{
            dingdingService.check(token,jsonString);
        }catch (Exception exp){
            log.warn("发送钉钉消息失败：",exp);
        }
        try {
        	 Map<String, Object> context = new HashMap<>();
             context.put("title", title);
             context.put("userName", userName);
             context.put("executeUerName", executeUerName);
             context.put("serverHost", serverHost);
             context.put("executeResult", executeResult);
             context.put("rejectReason", rejectReason);	
			EmailDo emailDo = new EmailDo();
			 emailDo.setTemplate("mail/dongjs_mail.vm.html");
			 List<String> to = Lists.newArrayList();
			 User user = userDao.findByNickname(userName);
			 String mail = user.getEmail();
			 System.out.println(mail);
           to.add(mail);//邮箱
           emailDo.setTo(to);
           List<String> cc = Lists.newArrayList();
           cc.add("1115170465@qq.com");
           emailDo.setCc(cc);
           emailDo.setSubject("DMS通知");
           emailDo.setContext(context);
           emailService.sendMain(emailDo);
		} catch (Exception e) {
			log.warn("发送邮箱消息失败：",e);
		}
    }
    /**
     * 发送dingding消息给dba
     * @param execution
     * @throws AppException
     */
    public void taskApply(ExecutionEntity execution) throws AppException {
        try {
            String processInstanceId = execution.getProcessInstanceId();
            int dsSize = 1;
            Object dsList = execution.getVariable("dsList");
            if(dsList != null){
                dsSize = ((List)dsList).size();
            }

            if(!processInstanceSize.containsKey(processInstanceId)){
                processInstanceSize.put(processInstanceId,dsSize);
            }

            if(!processInstanceOccur.containsKey(processInstanceId)){
                processInstanceOccur.put(processInstanceId,1);//init 的值是1
            }else{
                processInstanceOccur.put(processInstanceId,processInstanceOccur.get(processInstanceId) + 1);//增加1
            }

            if(processInstanceOccur.get(processInstanceId) >= processInstanceSize.get(processInstanceId)){//发消息的条件
                String processName = getProcessName(execution);
                String title = execution.getVariable("title") != null ? execution.getVariable("title").toString() : null;
                Pair<String, String> nameEnvPair = this.getNameEnvPair(execution);

                // subject
                StringBuilder subjectSb = new StringBuilder().append(DataSource.Env.getEnvName(nameEnvPair.getRight()))
                        .append('-').append(processName).append('-').append(nameEnvPair.getLeft());
                if (title != null) {
                    subjectSb.append(':').append(title);
                }
                String subject = subjectSb.toString();
                if(processInstanceSize.get(processInstanceId) > 1){
                    String appendSize = processInstanceSize.get(processInstanceId) + "";
                    subject = subject + "...等"+appendSize+"个数据源";
                }

                String applyUser = execution.getVariable("applyUser").toString();
                User user = userDao.findByUsername(applyUser);
                List mobileList = candidateUsersService.systemDBAMobileExclude(applyUser);
//                List mobileList = ImmutableList.of("13282128070","15968855093","18810735006","18757589409");//dba手机号，硬编码。
                combineMessageAndSend(dbaToken,user.getNickname(),"等待指定",mobileList,subject,"等待执行","",nameEnvPair.getRight(),processName);
                //清除历史数据
                processInstanceOccur.remove(processInstanceId);
                processInstanceSize.remove(processInstanceId);
            }
        } catch (ActivitiException | NullPointerException | JsonProcessingException e) {
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
            String comment = execution.getVariable("comment").toString();
            String executeUserName = execution.getVariable("assessorUser").toString();
            // agree
            String processName = getProcessName(execution);
            String applyUser = execution.getVariable("applyUser").toString();
            String title = execution.getVariable("title") != null ? execution.getVariable("title").toString() : null;
            Pair<String, String> nameEnvPair = this.getNameEnvPair(execution);

            // subject
            StringBuilder subjectSb = new StringBuilder().append(DataSource.Env.getEnvName(nameEnvPair.getRight()))
                    .append('-').append(processName).append('-').append(nameEnvPair.getLeft());
            if (title != null) {
                subjectSb.append(':').append(title);
            }
            String subject = subjectSb.toString();

            if (title != null) {
                subjectSb.append(':').append(title);
            }
//            List<User> users = userDao.findByUsernames(ImmutableList.of(applyUser,executeUserName));
            User user = userDao.findByUsername(applyUser);
            User executeUser = userDao.findByUsername(executeUserName);
            if(StringUtils.isBlank(user.getMobile())){
            	user.setMobile("lin");
            }
            List mobileList = ImmutableList.of(user.getMobile());
            if (approved == null
                    || "false".equalsIgnoreCase(approved.toString())
                    || "0".equalsIgnoreCase(approved.toString())) {
                combineMessageAndSend(devToken,user.getNickname(),executeUser.getNickname(),mobileList,subject,"申请被拒绝",comment,nameEnvPair.getRight(),processName);
            }else{
                String desc = "审核通过";
                if(processName.equals("结构变更")){
                    if(approved.toString().equals("2")){
                        String executeTime = execution.getVariable("executeTime").toString();
                        desc = desc + ",会在["+executeTime+"]执行";
                    }else{
                        //这种情况，不用钉钉通知
                        return;
                    }
                }else{
                    //noting to do
                }
                combineMessageAndSend(devToken,user.getNickname(),executeUser.getNickname(),mobileList,subject,desc,"",nameEnvPair.getRight(),processName);
            }
        } catch (ActivitiException | NullPointerException| JsonProcessingException e) {
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
            String executeUserName = "";
            if(execution.getVariable("assessorUser") != null)
                executeUserName = execution.getVariable("assessorUser").toString();
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
//            List<User> users = userDao.findByUsernames(ImmutableList.of(applyUser,executeUserName));
            User user = userDao.findByUsername(applyUser);
            User executeUser = null;
            if(!executeUserName.isEmpty())
                executeUser = userDao.findByUsername(executeUserName);
            String executeNickName = "";
            if(executeUser != null)
                executeNickName = executeUser.getNickname();
            List mobileList = ImmutableList.of(user.getMobile());
            combineMessageAndSend(devToken,user.getNickname(),executeNickName,mobileList,subject,"申请待调整",comment,nameEnvPair.getRight(),processName);
        } catch (ActivitiException | NullPointerException | JsonProcessingException e) {
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
            String processName = getProcessName(execution);
            String applyUser = execution.getVariable("applyUser").toString();
            String executeUserName = execution.getVariable("assessorUser").toString();
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
//            List<User> users = userDao.findByUsernames(ImmutableList.of(applyUser,executeUserName));
            User user = userDao.findByUsername(applyUser);
            User executeUser = userDao.findByUsername(executeUserName);
            List mobileList = ImmutableList.of(user.getMobile());
            if (executed == null) {
                combineMessageAndSend(devToken,user.getNickname(),executeUser.getNickname(),mobileList,subject,"被拒绝执行",comment,nameEnvPair.getRight(),processName);
            }else{
                String desc = "任务执行";
                String async = execution.getVariable("async").toString();
                if(async.equals("true")){
                    desc = desc + "中,点击查看执行进度信息";
                    return;//这里异步执行的情况 就只通知结果，执行开始不通知
                }else{
                    desc = desc + "结束,点击查看执行结果";
                }
                combineMessageAndSend(devToken,user.getNickname(),executeUser.getNickname(),mobileList,subject,desc,comment,nameEnvPair.getRight(),processName);
            }
        } catch (ActivitiException | NullPointerException| JsonProcessingException e) {
            throw new AppException(ExCode.WF_001, e);
        }
    }

    /*只处理异步执行的任务*/
    public void asyncStructChangeComplete(StructChange structChange) throws AppException{
        try {
            String executor = structChange.getExecutor();
            String dsEnv = structChange.getDsEnv();
            String comment = structChange.getReason();
            String dsName = structChange.getDsName();
            String processName = "结构变更";
            StringBuilder subjectSb = new StringBuilder().append(DataSource.Env.getEnvName(dsEnv))
                    .append('-').append(processName).append('-').append(dsName);

            Date execTimeDate = structChange.getExecuteTime();
            DateTime dateTime = new DateTime(execTimeDate.getTime());
            String execTimeStr = dateTime.toString("yyyy-MM-dd'T'HH:mm:ss");

            TaskBiz taskBiz = structChange.getTask();
            int task_id = taskBiz.getId();
            List<TaskBiz> taskBizs = taskBizDao.findByIds(ImmutableList.of(task_id));
            taskBiz = taskBizs.get(0);
            User applyUser= taskBiz.getStartUser();
            User realUser = userDao.findByUsername(applyUser.getUsername());
            List mobileList = ImmutableList.of(realUser.getMobile());


            String executeResult = "success";
            if(structChange.getExecuteStatus() == StructChange.ExecuteStatus.SUCCESS){
                executeResult = "success";
            }else if(structChange.getExecuteStatus() == StructChange.ExecuteStatus.FAIL){
                executeResult = "fail";
            }else if(structChange.getExecuteStatus() == StructChange.ExecuteStatus.TIMEOUT){
                executeResult = "timeout";
            }else if(structChange.getExecuteStatus() == StructChange.ExecuteStatus.TIMEOUT){
                executeResult = "timeout";
            }else if(structChange.getExecuteStatus() == StructChange.ExecuteStatus.ABORTED){
                executeResult = "aborted";
            }else{
                executeResult = "执行结果状态错误";
            }
            String desc = "任务[执行时间="+execTimeStr+"]执行完成[执行结果="+executeResult+"]";

            combineMessageAndSend(devToken,realUser.getNickname(),executor,mobileList,subjectSb.toString(),desc,comment,dsEnv,processName);
        }  catch (ActivitiException | NullPointerException| JsonProcessingException e) {
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
        ds = ds.split(";")[0];
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
