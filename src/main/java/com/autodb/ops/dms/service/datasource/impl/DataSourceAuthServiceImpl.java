package com.autodb.ops.dms.service.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceAuthDao;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.datasource.DataSourceRoleDao;
import com.autodb.ops.dms.repository.user.UserDao;
import com.autodb.ops.dms.service.datasource.DataSourceAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DataSourceAuthService Impl
 *
 * @author dongjs
 * @since 16/1/19
 */
@Service
public class DataSourceAuthServiceImpl implements DataSourceAuthService {
    @Autowired
    private DataSourceAuthDao dataSourceAuthDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DataSourceRoleDao dataSourceRoleDao;

    @Autowired
    private UserDao userDao;

    @Override
    public DataSourceAuth find(int id) throws AppException {
        return dataSourceAuthDao.find(id);
    }

    @Override
    public DataSourceAuth find(int userId, int dsId) throws AppException {
        return dataSourceAuthDao.find(userId, dsId);
    }

    @Override
    public List<DataSourceAuth> findByDs(int dsId) throws AppException {
        List<DataSourceAuth> authList = dataSourceAuthDao.findByDataSource(dsId);
        this.injectUser(authList);
        return authList;
    }

    @Override
    public List<DataSourceAuth> findByUserEnv(int userId, String env) throws AppException {
        List<DataSourceAuth> authList = dataSourceAuthDao.findByUserEnv(userId, env);
        this.injectDataSource(authList);
        return authList;
    }

    @Override
    public List<DataSourceAuth> findByUser(int userId) throws AppException {
        List<DataSourceAuth> authList = dataSourceAuthDao.findByUser(userId);
        this.injectDataSource(authList);
        return authList;
    }

    @Override
    @Transactional
    public int delete(List<Integer> ids) throws AppException {
        return this.dataSourceAuthDao.delete(ids);
    }

    @Override
    @Transactional
    public void add(DataSource dataSource, List<Integer> userIds, List<String> roles) throws AppException {
        Collection<User> users = userDao.findMap(userIds).values();
        for (User user : users) {
            add(dataSource, user, roles);
        }
    }

    @Override
    @Transactional
    public void add(DataSource dataSource, User user, List<String> roles) throws AppException {
        DataSourceAuth dataSourceAuth = dataSourceAuthDao.find(user.getId(), dataSource.getId());
        if (dataSourceAuth == null) {
            dataSourceAuth = new DataSourceAuth();
            dataSourceAuth.setDataSource(dataSource);
            dataSourceAuth.setUser(user);
            dataSourceAuth.setGmtAuth(new Date());
            this.dataSourceAuthDao.add(dataSourceAuth);
        }

        if (dataSourceAuth.getId() > 0) {
            // roles
            this.updateRoles(dataSourceAuth.getId(), roles);
        }
    }

    @Override
    @Transactional
    public void updateRoles(int id, List<String> roles) throws AppException {
        this.dataSourceAuthDao.deleteRoles(id);

        if (roles.size() > 0) {
            Map<String, DataSourceRole> codeMap = dataSourceRoleDao.findAllCodeMap();

            roles = roles.stream()
                    .map(codeMap::get)
                    .filter(role -> role != null)
                    .map(DataSourceRole::getCode)
                    .collect(Collectors.toList());

            if (roles.size() > 0) {
                this.dataSourceAuthDao.addRoles(id, roles);
            }
        }
    }

    @Override
    public boolean hasRole(int userId, int dsId, String role) throws AppException {
        return this.dataSourceAuthDao.hasRole(userId, dsId, role);
    }

    private void injectUser(List<DataSourceAuth> authList) {
        if (authList.size() > 0) {
            List<Integer> userIds = authList.stream().map(auth -> auth.getUser().getId()).collect(Collectors.toList());
            Map<Integer, User> userMap = userDao.findMap(userIds);

            authList.forEach(auth -> {
                User user = userMap.get(auth.getUser().getId());
                if (user != null) {
                    auth.setUser(user);
                }
            });
        }
    }

    private void injectDataSource(List<DataSourceAuth> authList) {
        if (authList.size() > 0) {
            List<Integer> dsIds = authList.stream().map(auth -> auth.getDataSource().getId()).collect(Collectors.toList());
            Map<Integer, DataSource> dataSourceMap = dataSourceDao.findMap(dsIds);

            authList.forEach(auth -> {
                DataSource dataSource = dataSourceMap.get(auth.getDataSource().getId());
                if (dataSource != null) {
                    auth.setDataSource(dataSource);
                }
            });
        }
    }
}
