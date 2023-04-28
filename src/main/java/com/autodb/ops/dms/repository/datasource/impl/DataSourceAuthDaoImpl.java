package com.autodb.ops.dms.repository.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.entity.datasource.DataSourceAuthRole;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.datasource.DataSourceAuthDao;
import com.autodb.ops.dms.repository.datasource.DataSourceRoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DataSourceAuthDao Impl
 * @author dongjs
 * @since 2015/12/29
 */
@Repository
public class DataSourceAuthDaoImpl extends SuperDao implements DataSourceAuthDao {
    @Autowired
    private DataSourceRoleDao dataSourceRoleDao;

    @Override
    public DataSourceAuth find(int id) throws AppException {
        return this.getSqlSession().selectOne("DataSourceAuthMapper.find", id);
    }

    @Override
    public DataSourceAuth find(int userId, int dsId) throws AppException {
        return this.getSqlSession().selectOne("DataSourceAuthMapper.findByUserDs", new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("dsId", dsId);
            }
        });
    }

    @Override
    public List<DataSourceAuth> findByDataSource(int dataSourceId) throws AppException {
        List<DataSourceAuth> authList = this.getSqlSession().selectList("DataSourceAuthMapper.findByDataSource", dataSourceId);
        injectRole(authList);
        return authList;
    }

    @Override
    public List<DataSourceAuth> findByDataSourceRole(int dataSourceId, String role) throws AppException {
        return this.getSqlSession().selectList("DataSourceAuthMapper.findByDataSourceRole", new HashMap<String, Object>() {
            {
                put("dataSourceId", dataSourceId);
                put("role", role);
            }
        });
    }

    @Override
    public List<DataSourceAuth> findByDataSourceRoles(int dataSourceId, List<String> roles) throws AppException {
        if (null == roles || roles.size() < 1) {
            return Collections.emptyList();
        }

        return this.getSqlSession().selectList("DataSourceAuthMapper.findByDataSourceRoles", new HashMap<String, Object>() {
            {
                put("dataSourceId", dataSourceId);
                put("roles", roles);
            }
        });
    }

    @Override
    public List<DataSourceAuth> findByUserEnv(int userId, String env) throws AppException {
        List<DataSourceAuth> authList = this.getSqlSession().selectList("DataSourceAuthMapper.findByUserEnv", new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("env", env);
            }
        });
        injectRole(authList);
        return authList;
    }

    @Override
    public List<DataSourceAuth> findByUser(int userId) throws AppException {
        List<DataSourceAuth> authList = this.getSqlSession().selectList("DataSourceAuthMapper.findByUser", userId);
        injectRole(authList);
        return authList;
    }

    @Override
    public void add(DataSourceAuth dataSourceAuth) throws AppException {
        this.getSqlSession().insert("DataSourceAuthMapper.add", dataSourceAuth);
    }

    @Override
    public int delete(List<Integer> ids) throws AppException {
        if (ids.size() < 1) {
            return 0;
        }
        this.getSqlSession().delete("DataSourceAuthMapper.deleteAuthRoleByAuthIds", ids);
        return this.getSqlSession().delete("DataSourceAuthMapper.deleteByIds", ids);
    }

    @Override
    public int deleteByDataSource(int dsId) throws AppException {
        this.getSqlSession().delete("DataSourceAuthMapper.deleteAuthRoleByDsId", dsId);
        return this.getSqlSession().delete("DataSourceAuthMapper.deleteByDataSource", dsId);
    }

    @Override
    public void addRoles(int id, List<String> roles) throws AppException {
        if (roles.size() < 1) {
            return;
        }
        this.getSqlSession().insert("DataSourceAuthMapper.addAuthRole", new HashMap<String, Object>() {
            {
                put("id", id);
                put("roles", roles);
            }
        });
    }

    @Override
    public int deleteRoles(int id) throws AppException {
        return this.getSqlSession().delete("DataSourceAuthMapper.deleteAuthRoleByAuthId", id);
    }

    @Override
    public boolean hasRole(int userId, int dsId, String role) throws AppException {
        return this.getSqlSession().selectOne("DataSourceAuthMapper.hasRole", new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("dsId", dsId);
                put("role", role);
            }
        }) != null;
    }

    private void injectRole(List<DataSourceAuth> authList) throws AppException {
        if (authList.size() < 1) {
            return;
        }

        // init struct
        List<Integer> authIds = authList.stream().map(DataSourceAuth::getId).collect(Collectors.toList());
        Map<Integer, List<DataSourceRole>> authRoles = authIds.stream()
                .collect(Collectors.toMap(id -> id, id -> new ArrayList<>()));

        // select
        List<DataSourceAuthRole> authRole = this.getSqlSession().selectList("DataSourceAuthMapper.findAuthRole", authIds);
        Map<String, DataSourceRole> roleMap = this.dataSourceRoleDao.findAllCodeMap();

        // compute
        authRole.forEach(dataSourceAuthRole -> {
            List<DataSourceRole> roles = authRoles.get(dataSourceAuthRole.getAuth());
            if (roles != null) {
                roles.add(roleMap.get(dataSourceAuthRole.getRole()));
            }
        });

        // inject
        authList.forEach(auth -> {
            List<DataSourceRole> roles = authRoles.get(auth.getId());
            roles.sort(Comparator.comparing(DataSourceRole::getOrder).reversed());
            auth.setRoles(roles);
        });
    }
}
