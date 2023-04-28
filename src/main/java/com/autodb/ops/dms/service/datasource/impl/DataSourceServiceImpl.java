package com.autodb.ops.dms.service.datasource.impl;

import com.autodb.ops.dms.common.AppContext;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.cache.LocalCache;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.domain.datasource.DataSourceManager;
import com.autodb.ops.dms.domain.datasource.observer.Change;
import com.autodb.ops.dms.domain.datasource.observer.DataSourceChange;
import com.autodb.ops.dms.domain.datasource.observer.DataSourceObserver;
import com.autodb.ops.dms.domain.datasource.visitor.ConnectionInfo;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.domain.datasource.visitor.Result;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceCobar;
import com.autodb.ops.dms.entity.datasource.DataSourceProxy;
import com.autodb.ops.dms.entity.user.OperateLog;
import com.autodb.ops.dms.entity.user.Role;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.entity.user.UserRole;
import com.autodb.ops.dms.repository.datasource.DataSourceAuthDao;
import com.autodb.ops.dms.repository.datasource.DataSourceCobarDao;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.datasource.DataSourceProxyDao;
import com.autodb.ops.dms.repository.task.StructChangeStashDao;
import com.autodb.ops.dms.repository.user.OperateLogDao;
import com.autodb.ops.dms.repository.user.RoleDao;
import com.autodb.ops.dms.repository.user.UserRoleDao;
import com.autodb.ops.dms.service.datasource.DataSourceAuthService;
import com.autodb.ops.dms.service.datasource.DataSourceService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * DataSourceService Impl
 *
 * @author dongjs
 * @since 2015/12/29
 */
@Service
public class DataSourceServiceImpl implements DataSourceService {
    private static Logger logger = LoggerFactory.getLogger(DataSourceServiceImpl.class);

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DataSourceAuthDao dataSourceAuthDao;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private DataSourceProxyDao dataSourceProxyDao;

    @Autowired
    private DataSourceCobarDao dataSourceCobarDao;

    @Autowired
    private OperateLogDao operateLogDao;

    @Autowired
    private StructChangeStashDao structChangeStashDao;

    @Autowired
    private DataSourceAuthService dataSourceAuthService;

    @Autowired
    private UserRoleDao userRoleDao;

    private ChangeObservable changeObservable = new ChangeObservable();

    @Autowired(required = false)
    private List<DataSourceObserver> dataSourceObservers;

    @PostConstruct
    public void init() {
        if (dataSourceObservers != null && dataSourceObservers.size() > 0) {
            dataSourceObservers.forEach(changeObservable::addObserver);
        }
    }

    @Override
    public DataSource find(int id) throws AppException {
        return dataSourceDao.find(id);
    }

    @Override
    public DataSource findByEnvName(String env, String name) throws AppException {
        return dataSourceDao.findByEnvName(env, name);
    }

    @Override
    public DataSource findByEnvSid(String env, String sid) throws AppException {
        return dataSourceDao.findByEnvSid(env, sid);
    }

    @Override
    public List<DataSource> findAll() throws AppException {
        return this.dataSourceDao.findAll();
    }

    @Override
    public List<DataSource> findByEnv(String env) throws AppException {
        return dataSourceDao.findByEnv(env);
    }

    @Override
    public List<DataSource> findUnAuthByUserEnv(int userId, String env) throws AppException {
        return dataSourceDao.findUnAuthByUserEnv(userId, env);
    }

    @Override
    @Transactional
    public int add(DataSource dataSource, boolean skipTest) throws AppException {
        dataSource.setEnv(DataSource.Env.getEnv(dataSource.getEnv()));

        // check
        if (dataSourceDao.findByEnvName(dataSource.getEnv(), dataSource.getName()) != null) {
            return 1;
        }
        /*if (dataSourceDao.findByEnvSid(dataSource.getEnv(), dataSource.getSid()) != null) {
            return 2;
        }*///同一个环境（production），允许相同的sid存在，但是数据源名称不一样xlp
        if (!skipTest && !isTestConnection(dataSource)) {
            return 3;
        }

        injectDatasourceProxy(dataSource);
        dataSource.setGmtCreate(new Date());
        dataSourceDao.add(dataSource);

        //给dba添加角色权限
        List<String> roleNames = ImmutableList.of("dev","exporter","reviewer","owner");
        List<UserRole> userRoles = userRoleDao.findAll();
        List<Integer> userIds = Lists.newArrayList();
        Set<Integer> userIdSet = Sets.newHashSet();
        for(UserRole userRole : userRoles){
            if(userRole.getRoleId() ==1 || userRole.getRoleId() == 2)
                userIdSet.add(userRole.getUserId());
        }
        userIds.addAll(userIdSet);
        dataSourceAuthService.add(dataSource, userIds, roleNames);
        // log
        String logStr = "进行数据源新增操作，新增数据源名称: " + dataSource.getName() + " 类型：" + dataSource.getType();
        OperateLog operateLog = OperateLog.of(dataSource, OperateLog.Type.DS_ADD, logStr);
        logger.info(operateLog.getOperator() + logStr);
        operateLogDao.add(operateLog);

        // notify
        changeObservable.notifyChange(DataSourceChange.of(dataSource, Change.ADD));
        return 0;
    }

    @Override
    @Transactional
    public int add(DataSource dataSource) throws AppException {
        return this.add(dataSource, false);
    }

    @Override
    @Transactional
    public int update(DataSource dataSource) throws AppException {
        dataSource.setEnv(DataSource.Env.getEnv(dataSource.getEnv()));

        // check
        DataSource ds = dataSourceDao.findByEnvName(dataSource.getEnv(), dataSource.getName());
        if (ds != null && !ds.getId().equals(dataSource.getId())) {
            return 1;
        }
        /*ds = dataSourceDao.findByEnvSid(dataSource.getEnv(), dataSource.getSid());
        if (ds != null && !ds.getId().equals(dataSource.getId())) {
            return 2;
        }*/
        if (!isTestConnection(dataSource)) {
            return 3;
        }

        DataSource old;
        if (ds != null && ds.getId().equals(dataSource.getId())) {
            old = ds;
        } else {
            old = dataSourceDao.find(dataSource.getId());
        }
        changeObservable.notifyChange(DataSourceChange.of(old, Change.DELETE));

        injectDatasourceProxy(dataSource);
        dataSource.setGmtModified(new Date());
        dataSourceDao.update(dataSource);

        // log
        String logStr = "进行数据源修改操作，修改数据源名称: " + dataSource.getName() + " 类型：" + dataSource.getType();
        OperateLog operateLog = OperateLog.of(dataSource, OperateLog.Type.DS_UPDATE, logStr);
        logger.info(operateLog.getOperator() + logStr);
        operateLogDao.add(operateLog);

        // notify
        changeObservable.notifyChange(DataSourceChange.of(dataSource, Change.ADD));
        return 0;
    }

    @Override
    public Pair<Pair<Boolean, String>, Pair<Boolean, String>> testConnection(DataSource dataSource) throws AppException {
        DataSourceEncryptUtils.encryptPassword(dataSource);
        ConnectionInfo main = dataSource.mainConnectionInfo();
        ConnectionInfo backup = dataSource.backupConnectionInfo();
        return Pair.of(dataSourceManager.getTempDatabaseVisitor(main).testConnection(main),
                dataSourceManager.getTempDatabaseVisitor(backup).testConnection(backup));
    }

    @Override
    public Pair<Boolean, String> testProxyConnection(DataSource dataSource) throws AppException {
        injectDatasourceProxy(dataSource);
        if (dataSource.getProxy() != null) {
            DataSourceEncryptUtils.encryptPassword(dataSource);
            ConnectionInfo proxy = dataSource.proxyConnectionInfo();
            return dataSourceManager.getTempDatabaseVisitor(proxy).testConnection(proxy);
        } else {
            return Pair.of(false, "broken proxy info");
        }
    }

    private void injectDatasourceProxy(DataSource dataSource) {
        DataSourceProxy proxy = dataSource.getProxy();
        if (proxy != null) {
            proxy = proxy.getId() != null ? dataSourceProxyDao.find(proxy.getId()) : null;
        }
        dataSource.setProxy(proxy);
    }

    @Override
    @Transactional
    public boolean delete(int id) throws AppException {
        boolean delete = false;
        DataSource dataSource = this.dataSourceDao.find(id);
        if (dataSource != null) {
            this.dataSourceAuthDao.deleteByDataSource(id);
            delete = this.dataSourceDao.delete(id);
            if (delete) {
                // log
                String logStr = "进行数据源删除操作，删除数据源名称: " + dataSource.getName() + " 类型：" + dataSource.getType();
                OperateLog operateLog = OperateLog.of(dataSource, OperateLog.Type.DS_DELETE, logStr);
                logger.info(operateLog.getOperator() + logStr);
                operateLogDao.add(operateLog);

                // notify
                changeObservable.notifyChange(DataSourceChange.of(dataSource, Change.ADD));
            }
        }
        return delete;
    }

    @Override
    public List<String> structNames(int id, String type) throws AppException {
        DataSource dataSource = this.dataSourceDao.find(id);
        return structNames(dataSource, type);
    }

    @Override
    public List<String> structNames(int userId, int id, String type) throws AppException {
        DataSource dataSource = this.dataSourceDao.findByUser(userId, id);
        return structNames(dataSource, type);
    }

    private List<String> structNames(DataSource dataSource, String type) throws AppException {
        try {
            if (dataSource != null) {
                DatabaseVisitor visitor = backupDatabaseVisitor(dataSource);

                List<String> names;
                if ("table".equalsIgnoreCase(type)) {
                    names = visitor.getTableNames();
                } else if ("view".equalsIgnoreCase(type)) {
                    names = visitor.getViewNames();
                } else {
                    names = Collections.emptyList();
                }

                return names;
            }
            return Collections.emptyList();
        } catch (SQLException e) {
            throw new AppException(ExCode.DS_001, e);
        }
    }

    @Override
    public Map<String, Object> structInfo(int id, String type, String name) throws AppException {
        DataSource dataSource = this.dataSourceDao.find(id);
        return this.structInfo(dataSource, type, name);
    }

    @Override
    public Map<String, Object> structInfo(int userId, int id, String type, String name) throws AppException {
        String structInfoCacheKey = id + "_"+type+"_"+name;
        Map<String, Object> structInfo = Maps.newHashMap();
        try {
            structInfo = (Map<String, Object>) LocalCache.get(structInfoCacheKey, new Callable() {
                @Override
                public Object call() throws Exception {
                    DataSource dataSource = dataSourceDao.findByUser(userId, id);
                    // operate log
                    String logStr = "进行表结构查询操作: " + dataSource.getName() + '-' + type + '-' + name;
                    OperateLog operateLog = OperateLog.of(dataSource, OperateLog.Type.STRUCT_QUERY, logStr);
                    logger.info(operateLog.getOperator() + logStr);
                    operateLogDao.add(operateLog);
                    Map<String, Object> tmpStructInfo = structInfo(dataSource, type, name);
                    //写到缓存里面
                    if(tmpStructInfo != null && tmpStructInfo.size() > 0){
                        LocalCache.put(structInfoCacheKey,tmpStructInfo);
                    }
                    return tmpStructInfo;
                }
            });
        }catch (ExecutionException exp){
           //log
        }
        return structInfo;
    }

    @Override
    public String showCreateTable(int dsId, String table) throws AppException {
        try {
            String sql = "";

            DataSource dataSource = dataSourceDao.find(dsId);
            if (dataSource != null) {
                DatabaseVisitor visitor = dataSourceManager.getDatabaseVisitor(dataSource.mainConnectionInfo());
                Result query = visitor.query("SHOW CREATE TABLE " + table);
                if (query.getData().size() > 0) {
                    sql = query.getData().get(0).get("Create Table").toString();
                }
            }
            return sql;
        } catch (SQLException e) {
            throw new AppException(ExCode.DS_001, e);
        }
    }

    @Override
    public int syncMainPwdAsBackup(String env) throws AppException {
        List<DataSource> dataSources = dataSourceDao.findByEnv(env);

        dataSources.forEach(dataSource -> {
            dataSource.setPassword2(DataSourceEncryptUtils.getDecryptPassword2(dataSource));
            dataSource.setPassword(dataSource.getPassword2());

            DataSourceEncryptUtils.encryptPassword(dataSource);
            dataSourceDao.update(dataSource);
        });

        logger.info("sync main password as backup password, env: {}, num; {}", env, dataSources.size());
        return dataSources.size();
    }

    @Override
    public Optional<String> schemaSql(String env, String sid) throws AppException {
        DataSource dataSource = dataSourceDao.findByEnvSid(env, sid);
        if (dataSource == null) {
            return Optional.empty();
        }

        DatabaseVisitor visitor = backupDatabaseVisitor(dataSource);
        try {
            String schemaSql = visitor.getTableNames()
                    .parallelStream()
                    .map(table -> {
                        try {
                            String sql = visitor.getTableCreateSql(table);
                            if (StringUtils.isNoneBlank(sql)) {
                                return new StringBuilder()
                                        .append("# Dump of table ").append(table).append('\n')
                                        .append("# ------------------------------------------------------------\n\n")
                                        .append(sql).append(";\n")
                                        .toString();
                            } else {
                                return null;
                            }
                        } catch (SQLException e) {
                            throw new AppException(ExCode.DS_001, e.getMessage());
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n\n"));

            // stash
            if (DataSource.Env.TEST.equals(dataSource.getEnv())) {
                String stashSql = structChangeStashDao.findByDatasource(dataSource.getId())
                        .stream()
                        .map(stash -> {
                            String sql = stash.getSql();
                            return sql.endsWith(";") ? sql : sql + ';';
                        }).collect(Collectors.joining("\n\n"));

                if (StringUtils.isNotEmpty(stashSql)) {
                    schemaSql += "\n\n\n# Stash schema sql\n# ------------------------------------------------------------\n\n";
                    schemaSql += stashSql;
                }
            }

            return Optional.ofNullable(schemaSql.isEmpty() ? null : schemaSql);
        } catch (SQLException e) {
            throw new AppException(ExCode.DS_001, e.getMessage());
        }
    }

    private Map<String, Object> structInfo(DataSource dataSource, String type, String name) throws AppException {
        Map<String, Object> info = new HashMap<>();
        try {
            if (dataSource != null) {
                DatabaseVisitor visitor = backupDatabaseVisitor(dataSource);

                if ("table".equalsIgnoreCase(type)) {
                    info.put("info", visitor.getTableInfo(name));
                    info.put("index", visitor.getTableIndex(name));
                    info.put("struct", visitor.getTableStruct(name));
                } else if ("view".equalsIgnoreCase(type)) {
                    info.put("info", visitor.getViewInfo(name));
                    info.put("index", visitor.getViewIndex(name));
                    info.put("struct", visitor.getViewStruct(name));
                }
            }
            return info;
        } catch (SQLException e) {
            throw new AppException(ExCode.DS_001, e.getMessage());
        }
    }

    private DatabaseVisitor backupDatabaseVisitor(DataSource dataSource) {
        DatabaseVisitor visitor = null;
        if (dataSource.isCobar()) {
            DataSourceCobar cobar = this.dataSourceCobarDao.findByDataSource(dataSource);
            if (cobar != null) {
                visitor = this.dataSourceManager.getDatabaseVisitor(cobar.backupConnectionInfo());
            }
        }
        if (visitor == null) {
            visitor = this.dataSourceManager.getDatabaseVisitor(dataSource.backupConnectionInfo());
        }

        return visitor;
    }

    private boolean isTestConnection(DataSource dataSource) throws AppException {
        Pair<Pair<Boolean, String>, Pair<Boolean, String>> test = testConnection(dataSource);

        return test.getLeft().getLeft() && test.getRight().getLeft();
    }

    /** Change Observable **/
    private static class ChangeObservable extends Observable {
        public void notifyChange(DataSourceChange dataSourceChange) {
            this.setChanged();
            this.notifyObservers(dataSourceChange);
        }
    }
}
