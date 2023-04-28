package com.autodb.ops.dms.service.task.impl;

import com.autodb.ops.dms.common.JSON;
import com.autodb.ops.dms.domain.activiti.TaskDingdingService;
import com.autodb.ops.dms.domain.activiti.TaskEmailService;
import com.autodb.ops.dms.entity.task.RuJob;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.data.pagination.Pagination;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.common.util.SqlUtils;
import com.autodb.ops.dms.domain.datasource.DataSourceManager;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.domain.flyway.FlywayService;
import com.autodb.ops.dms.domain.inception.InceptionService;
import com.autodb.ops.dms.domain.sqlcheck.SqlCheckChain;
import com.autodb.ops.dms.dto.caas.ChangeStash;
import com.autodb.ops.dms.dto.task.StructChangeApply;
import com.autodb.ops.dms.dto.task.StructChangeOnline;
import com.autodb.ops.dms.dto.task.StructStashOnline;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceCobar;
import com.autodb.ops.dms.entity.datasource.DataSourceOnline;
import com.autodb.ops.dms.entity.task.StructChange;
import com.autodb.ops.dms.entity.task.StructChangeStash;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceCobarDao;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.datasource.DataSourceOnlineDao;
import com.autodb.ops.dms.repository.task.StructChangeDao;
import com.autodb.ops.dms.repository.task.StructChangeStashDao;
import com.autodb.ops.dms.service.task.StructChangeService;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * StructChangeService Impl
 *
 * @author dongjs
 * @since 16/5/27
 */
@Service
public class StructChangeServiceImpl extends AbstractTaskService implements StructChangeService,InitializingBean {
    private static Logger log = LoggerFactory.getLogger(StructChangeServiceImpl.class);

    private static final String PROCESS_DEFINITION_KEY = "struct-change";

    @Autowired
    private StructChangeDao structChangeDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private DataSourceCobarDao dataSourceCobarDao;

    @Autowired
    private DataSourceOnlineDao dataSourceOnlineDao;

    @Autowired
    private InceptionService inceptionService;

    @Autowired
    @Qualifier("inceptionTestService")
    private InceptionService inceptionTestService;

    @Autowired
    private FlywayService flywayService;

    @Value("${data.ddl.timeout}")
    private long timeout = 86400000;

    @Value("${data.change.interval}")
    private int frequentInterval = 15;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SqlCheckChain sqlCheckChain;

    @Autowired
    private StructChangeStashDao structChangeStashDao;

    @Autowired
    private TaskDingdingService taskDingdingService;

    @Autowired
    private TaskEmailService taskEmailService;

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(4,16,60, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(32),new ThreadPoolExecutor.CallerRunsPolicy());

    private List<Integer> structChanges = Lists.newArrayList();
    private static ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(1);

    @Override
    public void afterPropertiesSet() throws Exception {
        scheduleExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                List<Integer> removedList = Lists.newArrayList();
                for(Integer structChangeId: structChanges){
                    try{
                        StructChange structChange = structChangeDao.findId(structChangeId);//每次从db重新查询数据
                        doProcess(structChange);
                        if(structChange.getExecuteStatus() != StructChange.ExecuteStatus.RUNNING){
                            removedList.add(structChangeId);
                        }
                    }catch (Exception e){
                        log.warn("周期检测异步结构变更状态异常:",e);
                    }
                }
                for(Integer structChangeId: removedList){
                    structChanges.remove(structChangeId);
                }
            }
        },3,10,TimeUnit.SECONDS);
    }

    @Override
    @Transactional
    public Pair<Integer, String> apply(User user, StructChangeApply structChangeApply) throws AppException {
        try {
            // 限制变更频率
            List<Integer> dsList = structChangeApply.getChanges().stream()
                    .map(StructChangeApply.Change::getDs)
                    .collect(Collectors.toList());
            /*if (structChangeDao.hasFrequentChange(user.getUsername(), dsList, frequentInterval)) {
                return Pair.of(3, null);
            }*/

            // validate ds
            Map<Integer, DataSource> dataSourceMap = dataSourceDao.findAuthByUser(user.getId()).stream()
                    .collect(Collectors.toMap(DataSource::getId, dataSource -> dataSource));
            structChangeApply.getChanges().forEach(change -> change.setDataSource(dataSourceMap.get(change.getDs())));

            List<DataSource> dataSourceList = structChangeApply.getChanges().stream()
                    .filter(change -> change.getDataSource() != null)
                    .map(StructChangeApply.Change::getDataSource)
                    .collect(Collectors.toList());

            // exists unknown ds
            if (dataSourceList.size() != structChangeApply.getChanges().size()) {
                return Pair.of(1, null);
            }

            List<Object> dsIdList = dataSourceList.stream().map(DataSource::getId).collect(Collectors.toList());//子任务通过里面的ds来执行对应的操作，这个值的构建比较重要
            List<String> dsNameList = dataSourceList.stream().map(DataSource::getName).collect(Collectors.toList());
            List<Callable<Triple<Boolean, String,StructChangeApply.Change>>> callables = Lists.newArrayList();

            for (StructChangeApply.Change change : structChangeApply.getChanges()) {//改成并行check
                callables.add(new Callable() {
                    @Override
                    public Triple<Boolean, String,StructChangeApply.Change> call() throws Exception {
                        Pair<Boolean, String> sqlCheck = check(change.getSql(), change.getType(), change.getDataSource());
                        return Triple.of(sqlCheck.getLeft(),sqlCheck.getRight(),change);
                    }
                });
            }

            List<Pair<Integer, String>> pairs = Lists.newArrayList();
            List<Future<Triple<Boolean, String,StructChangeApply.Change>>> results = null;
            try {
                results = executor.invokeAll(callables);
            } catch (InterruptedException e) {
                return Pair.of(2, "调用Inception被中断，请重试");
            }

            for(Future<Triple<Boolean, String,StructChangeApply.Change>> future : results){
                try {
                    Triple<Boolean, String,StructChangeApply.Change> sqlCheck = future.get();
                    if (sqlCheck.getLeft()) {
//                        sqlCheck.getRight().setSql(sqlCheck.getMiddle());//这里修改了sql的值
                    } else {
                        pairs.add(Pair.of(2, sqlCheck.getMiddle()));
                    }
                    if(future.isCancelled()){
                        future.cancel(true);
                    }
                } catch (Exception e) {
                    log.warn("executor.invokeAll Exception,",e);
                    pairs.add(Pair.of(2, "调用Inception失败，请重试"));
                    break;
                }
            }
            //对于还没有执行完的就全部取消
            for(Future<Triple<Boolean, String,StructChangeApply.Change>> future : results){
                if(!future.isDone()){
                    future.cancel(true);
                }
            }

            if(pairs.size() > 0)
                return pairs.get(0);//返回第一个错误
            // task biz
            TaskBiz taskBiz = new TaskBiz();
            taskBiz.setType(TaskBiz.Type.STRUCT_CHANGE);
            taskBiz.setStartUser(user);
            taskBiz.setStatus(TaskBiz.Status.PROCESS);
            taskBiz.setStartTime(new Date());
            taskBiz.setInfo(Joiner.on(',').join(dsNameList));
            taskBiz.setExplain(structChangeApply.getTitle());
            taskBizDao.add(taskBiz);

            Map<String,String> dsName = Maps.newHashMap();//用于保存各个子任务的数据源名称，便于在页面进行显示[TaskServiceImpl.ofTaskData]
            Map<String,String> dsEnvs = Maps.newHashMap();//用于保存各个子任务的数据源对应的环境，便于在页面进行显示[TaskServiceImpl.ofTaskData]

            structChangeApply.getChanges().forEach(change -> {
                DataSource ds = change.getDataSource();
                dsName.put(ds.getId()+"",ds.getName());
                dsEnvs.put(ds.getId()+"",ds.getEnv());
                /*if(ds.isCobar()){
                    dsIdList.remove(ds.getId());
                    DataSourceCobar cobar = this.dataSourceCobarDao.findByDataSource(ds);
                    for(DataSourceCobar.Sharding sharding : cobar.shardings()){
                        StructChange structChange = new StructChange();
                        DataSource oneNode = new DataSource();
                        oneNode.setSid(sharding.getName());
                        oneNode.setHost(sharding.getMasterHost());
                        oneNode.setPort(sharding.getMasterPort());
                        oneNode.setUsername(sharding.getMasterUserName());
                        oneNode.setPassword(sharding.getMasterPassword());
                        oneNode.setEnv(ds.getEnv());
                        oneNode.setName(sharding.getName());
                        oneNode.setId(ds.getId());//fixme
                        oneNode.setType(ds.getType());
                        oneNode.setCobar(false);

                        String key = cobar.getDataSource().getName()+"_"+sharding.getName();
                        String name = cobar.getDataSource().getName()+":"+sharding.getName();
                        //子任务通过里面的ds来执行对应的操作，这个值的构建比较重要，在需要的时候进行对应的规则解析
                        dsIdList.add(ds.getId()+";"+key);//这里只能通过这种方式把key传入runtime varibales;
                        dsName.put(ds.getId()+";"+key,name);
                        dsEnvs.put(ds.getId()+";"+key,ds.getEnv());

                        structChange.setTask(taskBiz);
                        structChange.setKey(key);
                        structChange.setDsEnv(oneNode.getEnv());
                        structChange.setDsName(name);
                        structChange.setReason(structChangeApply.getReason());
                        structChange.setChangeType(change.getType());
                        structChange.setSql(change.getSql());
                        generateReference(structChange, oneNode);
                        if (change.isOnline()) {
                            structChange.setOnline(true);
                            structChange.setLastChangeId(change.getLastChangeId());
                            structChange.setLastChangeTime(change.getLastChangeTime());
                        }
                        structChangeDao.add(structChange);
                    }

                    if(ds.getEnv().equals("test")){//qa和qa1_qa2同步
                        generate_qa_cobar_task(ds,change,taskBiz,structChangeApply.getReason(),dsIdList,dsName,dsEnvs);
                    }
                }else*/
                {
                    StructChange structChange = new StructChange();
                    structChange.setTask(taskBiz);
                    structChange.setKey(ds.getId().toString());
                    structChange.setDsEnv(ds.getEnv());
                    structChange.setDsName(ds.getName());
                    structChange.setReason(structChangeApply.getReason());
                    structChange.setChangeType(change.getType());
                    structChange.setSql(change.getSql());
                    generateReference(structChange, ds);
                    if (change.isOnline()) {
                        structChange.setOnline(true);
                        structChange.setLastChangeId(change.getLastChangeId());
                        structChange.setLastChangeTime(change.getLastChangeTime());
                    }
                    structChangeDao.add(structChange);

                    if(ds.getEnv().equals("test")){//qa和qa1_qa2同步
                        generate_qa_task(ds,change,taskBiz,structChangeApply.getReason(),dsIdList,dsName,dsEnvs);
                    }
                }
            });

            Map<String, Object> variables = new HashMap<>();
            variables.put("applyUser", user.getUsername());
            variables.put("dsList", dsIdList);
            variables.put("dsName", dsName);
            variables.put("dsEnvs", dsEnvs);
            variables.put("title", structChangeApply.getTitle());
//            variables.put("task_id",taskBiz.getId());
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

    private void generate_qa_cobar_task(DataSource originalDs,StructChangeApply.Change change,TaskBiz taskBiz,String reason,
                                        List<Object> dsIdList,Map<String,String> dsName,Map<String,String> dsEnvs){
        if(originalDs.getName().startsWith("qa")){//qa环境做处理
            String currentName = originalDs.getName();
            String sid = originalDs.getSid();
            List<DataSource> dataSourceList1 = dataSourceDao.findAllByEnvSid(originalDs.getEnv(),sid);
            if(dataSourceList1.size() > 1){//多余一个做处理
                for(DataSource ds : dataSourceList1){
                    if(ds.getName().equals(currentName))
                        continue;
                    dsName.put(ds.getId()+"",ds.getName());
                    dsEnvs.put(ds.getId()+"",ds.getEnv());
                    DataSourceCobar cobar = this.dataSourceCobarDao.findByDataSource(ds);//对cobar做处理
                    for(DataSourceCobar.Sharding sharding : cobar.shardings()){
                        StructChange structChange = new StructChange();
                        DataSource oneNode = new DataSource();
                        oneNode.setSid(sharding.getName());
                        oneNode.setHost(sharding.getMasterHost());
                        oneNode.setPort(sharding.getMasterPort());
                        oneNode.setUsername(sharding.getMasterUserName());
                        oneNode.setPassword(sharding.getMasterPassword());
                        oneNode.setEnv(ds.getEnv());
                        oneNode.setName(sharding.getName());
                        oneNode.setId(ds.getId());//fixme
                        oneNode.setType(ds.getType());
                        oneNode.setCobar(false);

                        String key = cobar.getDataSource().getName()+"_"+sharding.getName();
                        String name = cobar.getDataSource().getName()+":"+sharding.getName();
                        dsIdList.add(ds.getId()+";"+key);//这里只能通过这种方式把shardName传入runtime varibales;
                        dsName.put(ds.getId()+";"+key,name);
                        dsEnvs.put(ds.getId()+";"+key,ds.getEnv());

                        structChange.setTask(taskBiz);
                        structChange.setKey(key);
                        structChange.setDsEnv(oneNode.getEnv());
                        structChange.setDsName(name);
                        structChange.setReason(reason);
                        structChange.setChangeType(change.getType());
                        structChange.setSql(change.getSql());
                        generateReference(structChange, oneNode);
                        if (change.isOnline()) {
                            structChange.setOnline(true);
                            structChange.setLastChangeId(change.getLastChangeId());
                            structChange.setLastChangeTime(change.getLastChangeTime());
                        }
                        structChangeDao.add(structChange);
                    }
                }
            }
        }

    }



    private void generate_qa_task(DataSource ds,StructChangeApply.Change change,TaskBiz taskBiz,String reason,
                List<Object> dsIdList,Map<String,String> dsName,Map<String,String> dsEnvs){
        if(ds.getName().startsWith("qa")){//qa环境做处理
            String currentName = ds.getName();
            String sid = ds.getSid();
            List<DataSource> dataSourceList1 = dataSourceDao.findAllByEnvSid(ds.getEnv(),sid);
            if(dataSourceList1.size() > 1){//多余一个做处理
                Set<Integer> idSet = Sets.newHashSet();
                for(Object intId : dsIdList){
                    idSet.add((Integer) intId);
                }
                for(DataSource source : dataSourceList1){
                    if(source.getName().equals(currentName))
                        continue;
                    if(idSet.contains(source.getId()))
                        continue;
                    dsIdList.add(source.getId());
                    dsName.put(source.getId()+"",source.getName());
                    dsEnvs.put(source.getId()+"",source.getEnv());

                    StructChange structChange1 = new StructChange();
                    structChange1.setTask(taskBiz);
                    structChange1.setKey(source.getId().toString());
                    structChange1.setDsEnv(source.getEnv());
                    structChange1.setDsName(source.getName());
                    structChange1.setReason(reason);
                    structChange1.setChangeType(change.getType());
                    structChange1.setSql(change.getSql());
                    generateReference(structChange1, source);
                    if (structChange1.isOnline()) {
                        structChange1.setOnline(true);
                        structChange1.setLastChangeId(change.getLastChangeId());
                        structChange1.setLastChangeTime(change.getLastChangeTime());
                    }
                    structChangeDao.add(structChange1);
                }
            }
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
                    if(variables.get("ds").toString().split(";").length > 1){
                        ds = variables.get("ds").toString().split(";")[1];
                    }else {
                        ds = variables.get("ds").toString();
                    }
                    TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                    StructChange structChange = structChangeDao.findByTask(taskBiz.getId(), ds);

                    ds = variables.get("ds").toString();
                    DataSource dataSource = dataSourceDao.find(ds.split(";")[0]);

                    Pair<Boolean, String> sqlCheck = this.check(sql, structChange.getChangeType(), dataSource);
                    if (sqlCheck.getLeft()) {
                        sql = sqlCheck.getRight();
                    } else {
                        return Pair.of(2, sqlCheck.getRight());
                    }

                    structChange.setReason(reason);
                    structChange.setSql(sql);
                    generateReference(structChange, dataSource);
                    structChangeDao.update(structChange);
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
            log.warn("struct-export adjust error", e);
            return Pair.of(1, null);
        }
    }

    @Override
    public Optional<String> check(String sql, String env, String sid) throws AppException {
        DataSource dataSource = this.dataSourceDao.findByEnvSid(env, sid);
        if (dataSource != null) {
            Pair<Boolean, String> check = this.check(sql, StructChange.ChangeType.MIXED, dataSource);
            return check.getLeft() ? Optional.empty() : Optional.of(check.getRight());
        } else {
            return Optional.of("sid not exists");
        }
    }

    @Override
    @Transactional
    public Pair<Integer, String> stash(User user, ChangeStash stash) throws AppException {
        DataSource dataSource = dataSourceDao.findByUser(user.getId(), stash.getDs());
        if (dataSource == null || !DataSource.Env.TEST.equals(dataSource.getEnv())) {
            return Pair.of(1, "data source not exists");
        }

        Pair<Boolean, String> check = this.check(stash.getSql(), stash.getType(), dataSource);
        if (check.getLeft()) {
            structChangeStashDao.add(stash.toStructChangeStash(user));
            return Pair.of(0, "");
        } else {
            return Pair.of(2, check.getRight());
        }
    }

    @Override
    public List<StructChangeStash> onlineStash(int dataSourceId) throws AppException {
        // check exists online change
        if (structChangeDao.existInProcessOnline(String.valueOf(dataSourceId))) {
            return Collections.emptyList();
        }
        return structChangeStashDao.findByDatasource(dataSourceId);
    }

    /**
     * sql check
     *
     * @return true -> sql, false -> error reason
     */
    protected Pair<Boolean, String> check(String sql, byte changeType, DataSource dataSource) throws AppException {
        Triple<Boolean, List<String>, String> statements = SqlUtils
                .structChangeStatements(changeType, sql, dataSource.getType());//这个里面修改了sql的格式，暂时去掉这个功能
        if (!statements.getLeft()) {
            return Pair.of(false, statements.getRight());
        } else {
//            String newSql = statements.getMiddle().stream().collect(Collectors.joining(";\n\n")) + ';';

            Optional<String> error = inceptionCheck(sql, dataSource);
            // filter sql by innerSqlCheck
            /*Optional<String> error = innerSqlCheck(newSql, dataSource);
            if (!error.isPresent()) {
                // filter sql by inceptionService
                error = inceptionCheck(newSql, dataSource);
            }*/

            return error.isPresent() ? Pair.of(false, error.get()) : Pair.of(true, sql);
        }
    }

    @Override
    @Transactional
    public Pair<Integer, String> approve(String id, User assessor, int agree, String reason,Date execTime) throws AppException {
        try {
            Pair<Integer, String> result = null;
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(PROCESS_DEFINITION_KEY)
                    .taskDefinitionKey("audit")
                    .taskId(id)
                    .singleResult();
            if (task != null) {
                Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
                String ds = "";
                if(variables.get("ds").toString().split(";").length > 1){
                    ds = variables.get("ds").toString().split(";")[1];
                }else {
                    ds = variables.get("ds").toString();
                }
                TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                StructChange structChange = structChangeDao.findByTask(taskBiz.getId(), ds);

                if (agree < 0 || agree > 2) {
                    agree = 0;
                }

                structChange.setAssessor(assessor.getNickname());
                structChange.setAssessType(agree != 0 ? StructChange.AssessType.AGREE : StructChange.AssessType.REJECT);
                structChange.setAssessTime(new Date());
                structChange.setAssessRemark(reason);

                if(agree == 0){
                    structChange.setExecuteType(StructChange.ExecuteType.REJECT);
                }else if (agree == 1){//立即执行
                    structChange.setExecuteType(StructChange.ExecuteType.AGREE);
                }else if(agree == 2){//定时
                    structChange.setExecuteType(StructChange.ExecuteType.MANUAL);
                }


                Map<String, Object> vars = new HashMap<>();
                vars.put("approved", agree);
                vars.put("comment", reason);
                vars.put("assessorUser",assessor.getUsername());

                if(execTime == null){
                    execTime = new Date();
                }
                DateTime dateTime = new DateTime(execTime.getTime());
                vars.put("executeTime", dateTime.toString("yyyy-MM-dd'T'HH:mm:ss"));

                structChange.setExecutor(assessor.getNickname());
                structChange.setExecuteTime(dateTime.toDate());
                structChangeDao.update(structChange);
                /*if(agree)
                {//审核通过直接执行
                    vars.put("executed", agree);
                    vars.put("comment", reason);
                    variables.put("assessorUser",assessor.getUsername());
                    taskService.setVariables(task.getId(),variables);
                    result = innerExecute(id,variables,structChange,taskBiz.getExplain(),vars,assessor.getUsername(),task.getProcessInstanceId(),reason);//内部如果执行成功就会taskService.complete(id, vars);

                    structChange.setExecuteType(StructChange.ExecuteType.AGREE);
                    structChange.setExecutor(assessor.getNickname());
                    structChange.setExecuteTime(new Date());
                    structChange.setExecuteRemark(reason);
                    structChangeDao.update(structChange);
                }else*/
                {
                    taskService.claim(id, assessor.getUsername());
                    taskService.addComment(id, task.getProcessInstanceId(), reason);
                   /* if(StructChange.ChangeType.MIXED == structChange.getChangeType()){
                        result = Pair.of(-1, "");
                    }else if(StructChange.ChangeType.CREATE == structChange.getChangeType()){
                        result = Pair.of(-2, "");
                    }*/
                    result = Pair.of(-2, "");
                    taskService.complete(id, vars);//这里不执行完成的动作，再change里面来执行这个动作
                }
            } else {
                // task not find
                result = Pair.of(1, "task not find");
            }

            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("struct-change approve error", e);
            return Pair.of(1, null);
        }
    }

    @Deprecated
    private Pair<Integer, String> innerExecute(String id,Map<String, Object> variables,StructChange structChange,String taskBizExplain,Map<String, Object> vars
            ,String userName,String processId,String reason){
        try {
            Pair<Integer, String> result;
            DataSource dataSource = new DataSource();
            if (StructChange.ExecuteStatus.INIT == structChange.getExecuteStatus()) {
                String ds = variables.get("ds").toString();
                DataSource originalDataSource = dataSourceDao.find(ds.split(";")[0]);
                boolean envIsOnline = envIsOnline(originalDataSource.getEnv());
                if(originalDataSource.isCobar()){//找到对应的shard
                    DataSourceCobar cobar = this.dataSourceCobarDao.findByDataSource(originalDataSource);
                    if(cobar != null){
                        for(DataSourceCobar.Sharding sharding : cobar.shardings()){
                            String cobarShardName = originalDataSource.getName()+"_"+sharding.getName();
                            if(cobarShardName.equals(ds.split(";")[1])){
                                dataSource.setSid(sharding.getName());
                                dataSource.setHost(sharding.getMasterHost());
                                dataSource.setPort(sharding.getMasterPort());
                                dataSource.setUsername(sharding.getMasterUserName());
                                dataSource.setPassword(sharding.getMasterPassword());
                                dataSource.setEnv(originalDataSource.getEnv());
                                break;//只有唯一的一个
                            }
                        }
                    }
                }else{
                    dataSource = originalDataSource;//dataSourceDao.find(structChange.getKey());
                }

                boolean completeTask = true;
                if (StructChange.ChangeType.CREATE == structChange.getChangeType()) {
                    // 系统执行并且是only create类型
                    Optional<String> error = InceptionService.getError(inception(dataSource.getEnv()).execute(structChange.getSql(), dataSource,envIsOnline));
                    if (error.isPresent()) {
                        return Pair.of(2, error.get());
                    } else {
                        flywayService.structChange(dataSource, taskBizExplain, structChange);
                        structChange.setExecuteStatus(StructChange.ExecuteStatus.SUCCESS);
                    }
                } else {
                    // 系统执行并且是mixed类型, 进行异步执行
                    completeTask = false;
                    List<String> hash = inception(dataSource.getEnv()).asyncExecute(structChange.getSql(), dataSource,envIsOnline);
                    if(hash == null || hash.isEmpty()){//表示数据量小很快执行完了，导致incepion没有返回hash值
                        completeTask = true;
                        structChange.setExecuteStatus(StructChange.ExecuteStatus.SUCCESS);
                    }else{
                        structChange.setExecuteHash(JSON.objectToString(hash));
                        structChange.setExecuteStatus(StructChange.ExecuteStatus.RUNNING);
                    }
                }


                if (completeTask) {
                    result = Pair.of(0, null);
                    taskService.claim(id, userName);
                    taskService.addComment(id, processId, reason);
                    taskService.complete(id, vars);

                    // update DataSourceOnline info
                    if (structChange.isOnline()) {
                        updateOnlineInfo(dataSource, structChange);
                    }
                } else {
                    result = Pair.of(-1, null);
                }
            } else {
                result = Pair.of(2, "already running");
            }
            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("struct-export execute error", e);
            return Pair.of(1, null);
        }
    }

    @Override
    @Transactional
    public void change(DelegateExecution execution)  {
        try {
            String ds = execution.getVariable("ds").toString();
            String key = ds;
            if (ds.split(";").length > 1) {
                key = ds.split(";")[1];
            }
            TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(execution.getProcessInstanceId());
            StructChange structChange = structChangeDao.findByTask(taskBiz.getId(), key);
//            String agree = execution.getVariable("approved").toString();
            structChange(ds, structChange);

            execution.setVariable("executed", "true");
            if (structChange.getChangeType() == StructChange.ChangeType.MIXED) {
                execution.setVariable("async", "true");
            } else {
                execution.setVariable("async", "false");
            }
        }catch (Exception exp){
            log.warn("执行结构变更失败[change],",exp);
        }
    }

    protected void structChange(String ds,StructChange structChange) throws AppException{
        DataSource originalDataSource = dataSourceDao.find(ds.split(";")[0]);
        String cobarName = originalDataSource.getName();
        if(originalDataSource.isCobar()){
            DataSourceCobar cobar = this.dataSourceCobarDao.findByDataSource(originalDataSource);
            if(cobar != null){
                List<Callable<Object>> callables = Lists.newArrayList();
                for(DataSourceCobar.Sharding sharding : cobar.shardings()){
                    callables.add(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            StructChange childStructChange = null;
                            try {
                                Pair<StructChange,DataSource> childStructChangeAndDs = generateNewSDByShard(sharding,originalDataSource,cobar,structChange);
                                childStructChange = childStructChangeAndDs.getLeft();
                                doStructChange(childStructChangeAndDs.getLeft(),childStructChangeAndDs.getRight(),cobarName);
                            } catch (ActivitiException e) {
                                throw new AppException(ExCode.WF_001, e);
                            } catch (NullPointerException e) {
                                log.warn("struct-change execute error", e);
                                childStructChange.setExecuteStatus(StructChange.ExecuteStatus.FAIL);
                                childStructChange.setReference("NullPointerException");
                            }finally {
                                structChangeDao.add(childStructChange);
                            }
                            return childStructChange;
                        }
                    });
                }
                try {
                    executor.invokeAll(callables);
                    structChange.setExecuteStatus(StructChange.ExecuteStatus.SUCCESS);
                } catch (Exception e) {
                    log.warn("执行cobar结构变更失败,",e);
                    structChange.setExecuteStatus(StructChange.ExecuteStatus.FAIL);
                    structChange.setReference("执行cobar结构变更失败，联系管理员,异常="+e.getMessage());
                }finally {
                    structChangeDao.update(structChange);//更新状态信息
                    if(structChange.getExecuteStatus() == StructChange.ExecuteStatus.RUNNING){
                        structChanges.add(structChange.getId());//添加到定时检测队列里面
                    }
                }

            }
        }else{
            try {
                doStructChange(structChange,originalDataSource,cobarName);//如果是非cobar就用原来的structChange和originalDataSource
            } catch (ActivitiException e) {
                throw new AppException(ExCode.WF_001, e);
            } catch (NullPointerException e) {
                log.warn("struct-change execute error", e);
                structChange.setExecuteStatus(StructChange.ExecuteStatus.FAIL);
                structChange.setReference("NullPointerException");
            }finally {
                structChangeDao.update(structChange);//更新状态信息
                if(structChange.getExecuteStatus() == StructChange.ExecuteStatus.RUNNING){
                    structChanges.add(structChange.getId());//添加到定时检测队列里面
                }
            }
        }
    }

    private Pair<StructChange,DataSource> generateNewSDByShard(DataSourceCobar.Sharding sharding,DataSource ds, DataSourceCobar cobar, StructChange parentStructChange){
        Pair<StructChange,DataSource> result = null;
        StructChange structChange = new StructChange();
        DataSource oneNode = new DataSource();
        oneNode.setSid(sharding.getName());
        oneNode.setHost(sharding.getMasterHost());
        oneNode.setPort(sharding.getMasterPort());
        oneNode.setUsername(sharding.getMasterUserName());
        oneNode.setPassword(sharding.getMasterPassword());
        oneNode.setEnv(ds.getEnv());
        oneNode.setName(sharding.getName());
        oneNode.setId(ds.getId());//fixme
        oneNode.setType(ds.getType());
        oneNode.setCobar(false);

        String key = cobar.getDataSource().getName()+"_"+sharding.getName();
        String name = cobar.getDataSource().getName()+":"+sharding.getName();

        structChange.setAssessType(parentStructChange.getAssessType());
        structChange.setAssessor(parentStructChange.getAssessor());
        structChange.setExecutor(parentStructChange.getExecutor());
        structChange.setExecuteTime(parentStructChange.getExecuteTime());
        structChange.setExecuteType(parentStructChange.getExecuteType());

        structChange.setTask(parentStructChange.getTask());
        structChange.setKey(key);
        structChange.setDsEnv(oneNode.getEnv());
        structChange.setDsName(name);
        structChange.setReason(parentStructChange.getReason());
        structChange.setChangeType(parentStructChange.getChangeType());
        structChange.setSql(parentStructChange.getSql());
        generateReference(structChange, oneNode);
        if (parentStructChange.isOnline()) {
            structChange.setOnline(true);
            structChange.setLastChangeId(parentStructChange.getLastChangeId());
            structChange.setLastChangeTime(parentStructChange.getLastChangeTime());
        }

        result = Pair.of(structChange,oneNode);
        return result;
    }

    private void doStructChange(StructChange structChange,DataSource dataSource,String dbNameOrcobarName){
        if (StructChange.ExecuteStatus.INIT == structChange.getExecuteStatus()) {
            boolean envIsOnline = envIsOnline(dataSource.getEnv());
            boolean completeTask = true;
            if (StructChange.ChangeType.CREATE == structChange.getChangeType()) {
                // 系统执行并且是only create类型
                Optional<String> error = InceptionService.getError(inception(dataSource.getEnv()).execute(structChange.getSql(), dataSource,envIsOnline));
                if (error.isPresent()) {
                    structChange.setExecuteStatus(StructChange.ExecuteStatus.FAIL);
                    structChange.setExecuteRemark(error.get());
                } else {
                    flywayService.structChange(dataSource, structChange.getReason(), structChange);
                    structChange.setExecuteStatus(StructChange.ExecuteStatus.SUCCESS);
                }
                if(DataSource.Env.PROD.equals(dataSource.getEnv())){
                    notify_bi(dbNameOrcobarName,structChange.getSql());
                }
            } else {
                // 系统执行并且是mixed类型, 进行异步执行
                completeTask = false;
                List<String> hashList = inception(dataSource.getEnv()).asyncExecute(structChange.getSql(), dataSource,envIsOnline);
                if(hashList == null || hashList.isEmpty()){//表示数据量小很快执行完了，导致incepion没有返回hash值? 有些语句不会有hash值，inception分析满足osc才会返回hash
                    completeTask = true;
                    structChange.setExecuteStatus(StructChange.ExecuteStatus.SUCCESS);
                    structChange.setExecuteRemark("mixedddl执行完成");
//                    if(agree.equals("2"))//快速完成的任务通知
                    {
                        taskDingdingService.asyncStructChangeComplete(structChange);
                    }
                    if(DataSource.Env.PROD.equals(dataSource.getEnv())){
                        notify_bi(dbNameOrcobarName,structChange.getSql());
                    }
                }else{
                    String hashString = JSON.objectToString(hashList);
                    structChange.setExecuteHash(hashString);
                    structChange.setExecuteStatus(StructChange.ExecuteStatus.RUNNING);
                }
            }

            if (completeTask) {
                // update DataSourceOnline info
                if (structChange.isOnline()) {
                    updateOnlineInfo(dataSource, structChange);
                }
            }
        } else {
            structChange.setExecuteStatus(StructChange.ExecuteStatus.FAIL);
            structChange.setExecuteRemark("已经执行过，本次执行重复");
        }
    }

    private void notify_bi(String dbName,String sql){
        int type = type0fSql(sql);
        switch (type){
            case -2:
                break;
            case -1:
                taskEmailService.notify_bi("新建表",dbName,sql);
            case 0:
                taskEmailService.notify_bi("添加列",dbName,sql);
                break;
            case 1:
                taskEmailService.notify_bi("删除列",dbName,sql);
                break;
            case 2:
                taskEmailService.notify_bi("删除表",dbName,sql);
                break;
            case 3:
                taskEmailService.notify_bi("修改字段或表",dbName,sql);
                break;
        }
    }

    /**
     *
     * @param sql
     * @return -2 代表增加索引;-1 代表创建表; 0 代表 add column ;1 代表 删除column ;2 代表删除表; 3代表重命名
     */
    public int type0fSql(String sql){
        sql = sql.toLowerCase().replaceAll("\\s"," ");

        if(sql.contains("create table")){
            return -1;
        }

        if(sql.contains(" add ")){//这前后都有一个空格，避免表面里面有字符add
            if(sql.contains("add key") || sql.contains("add index") || sql.contains("add primary ") || sql.contains("add unique")){
                return -2;
            }else if(sql.contains("add column")){
                return 0;
            }else{
                return 0;//这里还不是很明确具体的类型
            }
        }

        if(sql.contains("drop")){
            if(sql.contains("drop table")){
                return 2;
            }else if(sql.contains("drop column")){
                return 1;
            }
        }

        if(sql.contains("rename")){
            return 3;
        }

        if(sql.contains("change")){
            return 3;
        }


        return -1;
    }
    /**
     * 这个方法现在没有用到,逻辑改到了innerExecute里面
     * @param id
     * @param executor
     * @param agree
     * @param reason
     * @return
     * @throws AppException
     */
    @Override
    @Transactional
    public Pair<Integer, String> execute(String id, User executor, byte agree, String reason) throws AppException {
        try {
            Pair<Integer, String> result;
            Task task = taskService.createTaskQuery()
                    .processDefinitionKey(PROCESS_DEFINITION_KEY)
                    .taskDefinitionKey("execute")
                    .taskId(id)
                    .singleResult();

            if (task != null) {
                Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
                String ds = variables.get("ds").toString();
                TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                if(ds.split(";").length > 1){
                    ds = ds.split(";")[1];
                }
                StructChange structChange = structChangeDao.findByTask(taskBiz.getId(), ds);
                DataSource dataSource = new DataSource();
                if (StructChange.ExecuteStatus.INIT == structChange.getExecuteStatus()) {
                    ds = variables.get("ds").toString();
                    DataSource originalDataSource = dataSourceDao.find(ds.split(";")[0]);
                    if(originalDataSource.isCobar()){//找到对应的shard
                        DataSourceCobar cobar = this.dataSourceCobarDao.findByDataSource(originalDataSource);
                        if(cobar != null){
                            for(DataSourceCobar.Sharding sharding : cobar.shardings()){
                                if(sharding.getName().equals(ds.split(";")[1])){
                                    dataSource.setSid(sharding.getName());
                                    dataSource.setHost(sharding.getMasterHost());
                                    dataSource.setPort(sharding.getMasterPort());
                                    dataSource.setUsername(sharding.getMasterUserName());
                                    dataSource.setPassword(sharding.getMasterPassword());
                                    break;//只有唯一的一个
                                }
                            }
                        }
                    }else{
                       dataSource = dataSourceDao.find(structChange.getKey());
                    }

                    boolean completeTask = true;

                    if (StructChange.ExecuteType.AGREE == agree) {
                        if (StructChange.ChangeType.CREATE == structChange.getChangeType()) {
                            // 系统执行并且是only create类型
                            Optional<String> error = InceptionService.getError(inception(dataSource.getEnv()).execute(structChange.getSql(), dataSource));
                            if (error.isPresent()) {
                                return Pair.of(2, error.get());
                            } else {
                                flywayService.structChange(dataSource, taskBiz.getExplain(), structChange);
                            }
                        } else {
                            // 系统执行并且是mixed类型, 进行异步执行
                            completeTask = false;
                            String hash = inception(dataSource.getEnv()).asyncExecute(structChange.getSql(), dataSource);
                            if(hash == null || hash.isEmpty()){//表示数据量小很快执行完了，导致incepion没有返回hash值
                                completeTask = true;
                            }else{
                                structChange.setExecuteHash(hash);
                                structChange.setExecuteStatus(StructChange.ExecuteStatus.RUNNING);
                            }
                        }
                    }

                    structChange.setExecutor(executor.getNickname());
                    structChange.setExecuteType(StructChange.toExecuteType(agree));
                    structChange.setExecuteTime(new Date());
                    structChange.setExecuteRemark(reason);
                    structChangeDao.update(structChange);

                    if (completeTask) {
                        taskService.claim(id, executor.getUsername());
                        taskService.addComment(id, task.getProcessInstanceId(), reason);
                        Map<String, Object> vars = new HashMap<>();
                        vars.put("executed", agree);
                        vars.put("comment", reason);
                        taskService.complete(id, vars);
                        result = Pair.of(0, null);

                        // update DataSourceOnline info
                        if (structChange.isOnline()) {
                            updateOnlineInfo(dataSource, structChange);
                        }
                    } else {
                        result = Pair.of(-1, null);
                    }
                } else {
                    result = Pair.of(2, "already running");
                }
            } else {
                result = Pair.of(1, "task not find");
            }

            return result;
        } catch (ActivitiException e) {
            throw new AppException(ExCode.WF_001, e);
        } catch (NullPointerException e) {
            log.warn("struct-export execute error", e);
            return Pair.of(1, null);
        }
    }

    @Transactional
    void updateOnlineInfo(DataSource dataSource, StructChange change) throws AppException {
        if (!change.isOnline()) {
            return;
        }

        if (DataSource.Env.PROD.equals(dataSource.getEnv())) {
            // online
            boolean create = false;
            DataSourceOnline online = dataSourceOnlineDao.findByDataSource(dataSource.getId());
            if (online == null) {
                create = true;
                online = new DataSourceOnline();
                online.setDataSource(dataSource);
            }

            online.setLastChangeId(change.getLastChangeId());
            online.setLastChangeTime(change.getLastChangeTime());
            online.setOnlineChangeId(change.getId());
            online.setOnlineTime(change.getExecuteTime());

            if (create) {
                dataSourceOnlineDao.add(online);
            } else {
                dataSourceOnlineDao.update(online);
            }
        } else if (DataSource.Env.TEST.equals(dataSource.getEnv())) {
            // stash
            structChangeStashDao.deleteByLastId(dataSource.getId(), change.getLastChangeId());
        }
    }

    @Override
    @Transactional
    public Pair<Integer, String> progress(String id) throws AppException {
        Pair<Integer, String> result = null;
        {
          /*  Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
            String dsId = variables.get(task.getProcessInstanceId()).toString();

            TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());*/
            //StructChange structChange = structChangeDao.findByTask(taskBiz.getId(), dsId);
            StructChange structChange = structChangeDao.findId(Integer.parseInt(id));
            result = doProcess(structChange);
        }
        /*else {
            result = Pair.of(0, "ddl task not exists");
        }*/
        return result;
    }

    public Pair<Integer, String> cancelProgress(String id) throws AppException{
        Pair<Integer, String> result = null;
        String resultStr = "";
        StructChange structChange = null;
        try {
            structChange = structChangeDao.findId(Integer.parseInt(id));
            if(structChange.getExecuteStatus() != StructChange.ExecuteStatus.RUNNING){
                result = Pair.of(0,"任务已经运行完成");
                return result;
            }
            String hash = structChange.getExecuteHash();
            String dsEnv = structChange.getDsEnv();
            boolean envIsOnline = envIsOnline(dsEnv);
            String dataSourceName = structChange.getDsName();
            String sql = structChange.getSql();//从sql里面提取表名称

            DataSource dataSource = null;
            if (dataSourceName.contains(":")) {//cobar datasource
                String cobarName = dataSourceName.split(":")[0];//cobarName==dsName
                String shardName = dataSourceName.split(":")[1];
                dataSource = dataSourceDao.findByEnvName(dsEnv, cobarName);
                DataSource oneNode = new DataSource();
                if (dataSource.isCobar()) {//再次验证是cobar类型,
                    DataSourceCobar dataSourceCobar = dataSourceCobarDao.findByDataSource(dataSource);
                    for (DataSourceCobar.Sharding sharding : dataSourceCobar.shardings()) {
                        if (sharding.getName().equals(shardName)) {
                            oneNode.setSid(sharding.getName());
                            oneNode.setHost(sharding.getMasterHost());
                            oneNode.setPort(sharding.getMasterPort());
                            oneNode.setUsername(sharding.getMasterUserName());
                            oneNode.setPassword(sharding.getMasterPassword());
                            oneNode.setEnv(dsEnv);
                            oneNode.setName(sharding.getName());
//                    oneNode.setId(ds.getId());//fixme
                            oneNode.setType(dataSource.getType());
                            oneNode.setCobar(false);
                            break;
                        }
                    }
                }
                dataSource = oneNode;
            } else {//datasource
                dataSource = dataSourceDao.findByEnvName(dsEnv, dataSourceName);
            }
            //1,通过inception取消
            InceptionService inception = inception(dataSource.getEnv());

            List<String> hashList = JSON.parseObject(hash,List.class);
            for(String hashStr : hashList){
                InceptionService.ProgressResult progressResult = inception.cancelProgress(hashStr, envIsOnline);
                resultStr = progressResult.getMessages();
                result = Pair.of(0,resultStr);//fixme 这里的结果合并
            }
            //2,到对应的datasource删除triggers
//        $2代表库名，$3代表 表名
//        use $2;
//        drop trigger if exists pt_osc_$2_$3_del;
//        drop trigger if exists pt_osc_$2_$3_upd;
//        drop trigger if exists pt_osc_$2_$3_ins;
//        drop table if exists _$3_new;
            String dbName = dataSource.getSid();
            List<String> tableNames = getTableNameFromSql(sql);
            StringBuilder triggerStringBuilder = new StringBuilder();

            for (String tableName : tableNames) {
                String value = dbName + "_" + tableName;
                String del = "drop trigger if exists pt_osc_value_del;";
                String upd = "drop trigger if exists pt_osc_value_upd;";
                String ins = "drop trigger if exists pt_osc_value_ins;";
                String newSql = "drop table if exists _tableName_new;";

                del = del.replace("value", value);
                upd = upd.replace("value", value);
                ins = ins.replace("value", value);
                newSql = newSql.replace("tableName", tableName);

                triggerStringBuilder.append(del).append(upd).append(ins).append(newSql);
            }
            log.info("删除trigger的语句:" + triggerStringBuilder.toString());
            DatabaseVisitor databaseVisitor = dataSourceManager.getDatabaseVisitor(dataSource.mainConnectionInfo());
            int count = databaseVisitor.update(triggerStringBuilder.toString());
            /*Optional<String> error = InceptionService.getError(inception(dataSource.getEnv()).execute(triggerStringBuilder.toString(), dataSource, envIsOnline));
            if (error.isPresent()) {
                resultStr = resultStr + ";清理trigger失败,需要dba手动执行。";
            } else {
                resultStr = resultStr + ";清理trigger成功。";
            }*/
            //钉钉通知
            structChange.setExecuteStatus(StructChange.ExecuteStatus.ABORTED);
            structChangeDao.update(structChange);
            taskDingdingService.asyncStructChangeComplete(structChange);
        }catch (Exception exp){
            result = Pair.of(1,resultStr+";异常="+exp.getMessage());
        }
        return result;
    }

    /**
     *
     * 从已知的sql语句里面获取tableName
     * 规则是 table后面的表名称
     * @param sql
     * @return
     */
    public List<String> getTableNameFromSql(String sql){
        List<String> tableNames = Lists.newArrayList();
        String lowerCase = sql.toLowerCase();
        String[] items = lowerCase.split("\\s+");//空格拆分
        String preItem = "";
        for(String item : items){
            if(preItem.equals("table")){
                if(!Strings.isNullOrEmpty(item)){
                    //特殊处理,去掉.、`
                    if(item.contains("."))
                        item = item.split("\\.")[1];
                    item = item.replace("`","").trim();
                    tableNames.add(item);
                }
            }
            preItem = item;
        }
        return tableNames;
    }

    //fixme 是否需要synchronized
    synchronized Pair<Integer, String> doProcess(StructChange structChange){
        Pair<Integer, String> result;
        if (StructChange.ExecuteStatus.RUNNING == structChange.getExecuteStatus()
                && StringUtils.isNotEmpty(structChange.getExecuteHash())) {
            boolean checkTimeout = false;
            boolean submitTask = false;

            boolean envIsOnline = envIsOnline(structChange.getDsEnv());
            String hashString = structChange.getExecuteHash();
            List<String> hashList = Lists.newArrayList();
            if(hashString.contains("[")){//是新的类型 兼容之前的
                hashList.addAll(JSON.parseObject(hashString, List.class));
            }else{//老的string
                hashList.add(hashString);
            }

            InceptionService.ProgressResult progress = null;
            for(String hash : hashList){//取最小的那个
                InceptionService.ProgressResult tmpProgress = inception(structChange.getDsEnv())
                        .execProgress(hash,envIsOnline);
                if(progress == null || tmpProgress.getStatus() <= progress.getStatus()){
                    if(progress != null &&
                        tmpProgress.getStatus() == progress.getStatus() &&
                        tmpProgress.getStatus() == InceptionService.ProgressResult.Status.RUNNING){
                        if(tmpProgress.getMessages().equals("init")){
                            continue;
                        }
                    }
                    progress = tmpProgress;
                }
            }

            DataSource dataSource = null;
            String desEnv = structChange.getDsEnv();
            String dsName = structChange.getDsName().split(":")[0];
            if (NumberUtils.isNumber(structChange.getKey())){
                dataSource = dataSourceDao.find(structChange.getKey());//key如果是单个数字就是数据源的id，如果不是就是拼接的，比如数据源名称加上shard名称
            }else{
                dataSource = dataSourceDao.findByEnvName(desEnv,dsName);//这种方式靠谱
            }
            switch (progress.getStatus()) {
                case InceptionService.ProgressResult.Status.SUCCESS:
                    structChange.setExecuteStatus(StructChange.ExecuteStatus.SUCCESS);
                    try {
                        flywayService.structChange(dataSource, structChange.getDsName(), structChange);//fixme  structChange.getDsName()代替 taskBiz.getExplain()
                    }catch (Exception exp){
                        log.warn("flywayService.structChange失败，异常:",exp);
                    }
                    //structChange.setReference(StringUtils.defaultIfEmpty(getFinalMessages(progress.getFinalMessages()),"ddl execute success"));
                    structChange.setExecuteRemark(StringUtils.defaultIfEmpty(progress.getMessages(), "ddl execute success"));
                    //如果是中止任务，这里progress.getMessages()会包含"Execute Aborted"
                    if(!Strings.isNullOrEmpty(progress.getMessages())){
                        if(progress.getMessages().contains("Execute Aborted")){//修改执行状态为取消
                            structChange.setExecuteStatus(StructChange.ExecuteStatus.ABORTED);
                        }
                    }
                    result = Pair.of(0, StringUtils.defaultIfEmpty(getFinalMessages(progress.getFinalMessages()),
                            "ddl execute success"));
                    submitTask = true;

                    //只有当执行成功 发邮件通知bi
                    if(DataSource.Env.PROD.equals(dataSource.getEnv())){
                        notify_bi(structChange.getDsName(),structChange.getSql());//structChange.getDsName()如果是cobar的话会是cobar:shardx这个形式
                   }

                    break;
                case InceptionService.ProgressResult.Status.FAIL:
                    structChange.setExecuteStatus(StructChange.ExecuteStatus.FAIL);
                    structChange.setExecuteRemark(StringUtils.defaultIfEmpty(progress.getMessages(), "未知原因，联系dba"));
                    result = Pair.of(0, StringUtils.defaultIfEmpty(progress.getMessages(), "ddl execute fail"));
                    submitTask = true;
                    break;
                default:
                    checkTimeout = true;
                    result = Pair.of(1, progress.getMessages());
            }

            // timeout
            if (checkTimeout && (System.currentTimeMillis() - structChange.getExecuteTime().getTime() > timeout)) {
                structChange.setExecuteStatus(StructChange.ExecuteStatus.TIMEOUT);
                result = Pair.of(0, "ddl execute timeout");
                submitTask = true;
            }

            // submit task
            if (submitTask) {
                //这里更新的时候先检查是否已经被中止，如果已经被中止，这里就不再更新，不然状态会不一致
                StructChange oldStructChange = structChangeDao.findId(structChange.getId());
                if(oldStructChange.getExecuteStatus() == StructChange.ExecuteStatus.RUNNING){//不覆盖数据库的结果状态
                    structChangeDao.update(structChange);
                    taskDingdingService.asyncStructChangeComplete(structChange);
                }else{
                    //nothing to do
                }

                if (structChange.isOnline()) {
                    updateOnlineInfo(dataSource, structChange);
                }
            }
        } else {
            result = Pair.of(0, "ddl已经执行完成或者未执行点击刷新页面");
        }
        return result;
    }

    private String getFinalMessages(List<InceptionService.Result> finalMessages) {
        if (finalMessages == null || finalMessages.size() < 1) {
            return null;
        } else {
            try {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalMessages);
            } catch (JsonProcessingException e) {
                return null;
            }
        }
    }

    @Override
    @Transactional
    public int result(User user, String id) throws AppException {
        return this.completeTask(user, "result", id);
    }

    @Override
    public List<StructChange> onlineChanges(int dataSourceId, int endChangeId) throws AppException {
        List<StructChange> result = Collections.emptyList();
        // check exists online change
        if (structChangeDao.existInProcessOnline(String.valueOf(dataSourceId))) {
            return result;
        }

        DataSource prod = dataSourceDao.find(dataSourceId);
        if (prod != null && DataSource.Env.PROD.equals(prod.getEnv())) {
            DataSource test = dataSourceDao.findByEnvSid(DataSource.Env.TEST, prod.getName());
            if (test != null) {
                DataSourceOnline online = dataSourceOnlineDao.findByDataSource(prod.getId());
                Pagination pagination = new Pagination();
                pagination.setPageSize(Pagination.MAX_PAGE_SIZE);
                Page<StructChange> page = new Page<>(pagination);
                List<StructChange> changes = structChangeDao.findByOnline(test.getId().toString(), online, page);
                List<Integer> taskIds = changes.stream()
                        .map(change -> change.getTask().getId())
                        .collect(Collectors.toList());

                Map<Integer, TaskBiz> taskBizMap = taskBizDao.findByIds(taskIds)
                        .stream()
                        .collect(Collectors.toMap(TaskBiz::getId, task -> task));

                result = changes.stream()
                        .peek(change -> change.setTask(taskBizMap.get(change.getTask().getId())))
                        .filter(change -> Objects.nonNull(change.getTask()) && change.getId() <= endChangeId)
                        .collect(Collectors.toList());
                page.setData(result);
            }
        }
        return result;
    }

    @Override
    public List<TaskBiz> inProcessOnline(int dataSourceId) throws AppException {
        return structChangeDao.inProcessOnline(String.valueOf(dataSourceId));
    }

    @Override
    public Pair<Integer, String> online(User user, StructStashOnline online) throws AppException {
        List<StructChangeStash> stashes = onlineStash(online.getDs())
                .stream()
                .filter(stash -> stash.getId() <= online.getTask())
                .collect(Collectors.toList());

        if (stashes.isEmpty()) {
            return Pair.of(1, null);
        }

        // prepare
        Byte changeType = stashes.stream()
                .filter(stash -> stash.getChangeType() != StructChange.ChangeType.CREATE)
                .map(StructChangeStash::getChangeType)
                .findFirst()
                .orElse(StructChange.ChangeType.CREATE);
        String reason = stashes.stream()
                .map(StructChangeStash::getTitle)
                .collect(Collectors.joining("\n"));
        StructChangeStash lastStash = stashes.get(stashes.size() - 1);
        StructChangeApply.Change change = new StructChangeApply.Change(online.getDs(), changeType, online.getSql());
        change.setOnline(true);
        change.setLastChangeId(lastStash.getId());
        change.setLastChangeTime(lastStash.getGmtCreate());
        List<StructChangeApply.Change> changeList = Collections.singletonList(change);

        // apply
        StructChangeApply apply = new StructChangeApply();
        apply.setApplyUser(user);
        apply.setTitle(Constants.STASH_FLAG
                + (StringUtils.isNotBlank(online.getTitle()) ? online.getTitle() : Constants.STASH_TITLE));
        apply.setReason(reason.length() > 1000 ? reason.substring(1000) : reason);
        apply.setChanges(changeList);

        return apply(user, apply);
    }

    @Override
    public Pair<Integer, String> online(User user, StructChangeOnline online) throws AppException {
        List<StructChange> changes = onlineChanges(online.getDs(), online.getTask());

        if (changes.isEmpty()) {
            return Pair.of(1, null);
        }

        // prepare
        Byte changeType = changes.stream()
                .filter(change -> change.getChangeType() != StructChange.ChangeType.CREATE)
                .map(StructChange::getChangeType)
                .findFirst()
                .orElse(StructChange.ChangeType.CREATE);
        String reason = changes.stream()
                .map(change -> change.getTask().getExplain())
                .collect(Collectors.joining("\n"));
        StructChangeApply.Change change = new StructChangeApply.Change(online.getDs(), changeType, online.getSql());
        StructChange lastStructChange = changes.get(changes.size() - 1);
        change.setOnline(true);
        change.setLastChangeId(lastStructChange.getId());
        change.setLastChangeTime(lastStructChange.getExecuteTime());
        List<StructChangeApply.Change> changeList = Collections.singletonList(change);

        // apply
        StructChangeApply apply = new StructChangeApply();
        apply.setApplyUser(user);
        apply.setTitle(Constants.ONLINE_FLAG
                + (StringUtils.isNotBlank(online.getTitle()) ? online.getTitle() : Constants.ONLINE_TITLE));
        apply.setReason(reason.length() > 1000 ? reason.substring(1000) : reason);
        apply.setChanges(changeList);

        return apply(user, apply);
    }

    @Override
    protected Object findEntityByTask(int taskId) {
        return structChangeDao.findByTask(taskId);
    }

    @Override
    protected Object findEntityByTask(int taskId, String key) {
        return structChangeDao.findByTask(taskId, key);
    }

    @Override
    protected String getProcessDefinitionKey() {
        return PROCESS_DEFINITION_KEY;
    }

    /** generate StructChange reference **/
    private void generateReference(StructChange structChange, DataSource dataSource) {
        try {
            if (structChange.getChangeType() == StructChange.ChangeType.MIXED) {
                StringBuilder sb = new StringBuilder();
                // read_only
                DatabaseVisitor visitor = mainDatabaseVisitor(dataSource);
                sb.append("read_only: ").append(visitor.getVariable("read_only")).append("\n\n");

                // table size
                List<String> tableNames = SqlUtils.getAlterTableNames(structChange.getSql(), dataSource.getType());
                for (String tableName : tableNames) {
                    double tableSize = visitor.getTableSize(tableName) / (1024.0 * 1024.0);
                    sb.append("<a href=\"javascript:;\" show-sql=\"true\" data-id=\"").append(dataSource.getId()).append("\">")
                            .append(tableName).append("</a>")
                            .append(": ").append(tableSize).append("MB\n");
                }

                if (sb.length() > 0) {
                    structChange.setReference(sb.toString());
                }
            }
        } catch (SQLException e) {
            // ignore
            log.warn("struct change generate reference error", e);
        }
    }

    private DatabaseVisitor mainDatabaseVisitor(DataSource dataSource) {
        DatabaseVisitor visitor = null;
        if (dataSource.isCobar()) {
            DataSourceCobar cobar = this.dataSourceCobarDao.findByDataSource(dataSource);
            if (cobar != null) {
                visitor = this.dataSourceManager.getDatabaseVisitor(cobar.mainConnectionInfo());
            }
        }
        if (visitor == null) {
            visitor = this.dataSourceManager.getDatabaseVisitor(dataSource.mainConnectionInfo());
        }

        return visitor;
    }

    private InceptionService inception(String env) {
        return DataSource.Env.PROD.equals(env) ? inceptionService : inceptionTestService;
    }

    private Optional<String> inceptionCheck(String sql, DataSource dataSource) {
        InceptionService inception = inception(dataSource.getEnv());
        boolean envIsOnline = envIsOnline(dataSource.getEnv());
        Optional<String> error = null;
        if (dataSource.isCobar()) {
            DataSourceCobar cobar = this.dataSourceCobarDao.findByDataSource(dataSource);
            if (cobar != null) {
                for(DataSourceCobar.Sharding sharding : cobar.shardings()){//每一个节点都检查
                    DataSource tmpSource = new DataSource();
                    tmpSource.setSid(sharding.getName());
                    tmpSource.setHost(sharding.getSlaveHost());
                    tmpSource.setPort(sharding.getSlavePort());
                    tmpSource.setUsername(sharding.getSlaveUserName());
                    tmpSource.setPassword(sharding.getSlavePassword());
                    error = InceptionService.getError(inception.check(sql, tmpSource, cobar.getMetadata(),envIsOnline));
                    if(error.isPresent())
                        break;
                }
            }

        }
        if (error == null) {
            error = InceptionService.getError(inception.check(sql, dataSource,envIsOnline));
        }

        return error;
    }

    public Pair<Integer, String> change_exec_time(String definitionKey, String id,Date execTime){
        Pair<Integer, String> result = null;
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(getProcessDefinitionKey())
                .taskDefinitionKey(definitionKey)
                .taskId(id)
                .singleResult();
        if(task != null){
            try {
                DateTime dateTime = new DateTime(execTime.getTime());
                String executionId = task.getExecutionId();//通过execution_id索引查找记录 fixme 确保每个任务的execution_id是唯一的
                //更新时间到数据库的act_ru_job里面
                RuJob ruJob = ruJobDao.findByExecutionId(executionId);
                if(ruJob != null){
                    Timestamp timestamp = new Timestamp(dateTime.getMillis());
                    ruJob.setDuedate(timestamp);
                    ruJobDao.update(ruJob);
                }

                Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
                variables.put("executeTime", dateTime.toString("yyyy-MM-dd'T'HH:mm:ss"));
                runtimeService.setVariables(task.getExecutionId(), variables);//更新时间到runtime里面

                TaskBiz taskBiz = taskBizDao.findByProcessInstanceId(task.getProcessInstanceId());
                List<StructChange> structChanges = (List<StructChange>)findEntityByTask(taskBiz.getId());
                for(StructChange structChange : structChanges){
                    structChange.setExecuteTime(execTime);
                    structChangeDao.update(structChange);
                }
                result = Pair.of(1, "更新成功");
            }catch (Exception exp){
                result = Pair.of(2, "更新失败"+exp.getMessage());
            }
        }else {
            result = Pair.of(-1,"任务不存在");
        }
        return result;
    }

    private boolean envIsOnline(String env){
        return DataSource.Env.PROD.equals(env) ? true : false;
    }

    private Optional<String> innerSqlCheck(String sql, DataSource dataSource) {
        return sqlCheckChain.check(sql, dataSource);
    }
}
