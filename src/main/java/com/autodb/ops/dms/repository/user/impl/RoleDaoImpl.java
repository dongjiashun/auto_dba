package com.autodb.ops.dms.repository.user.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.user.Role;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.user.RoleDao;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * role dao
 * @author dongjs
 * @since 2015/11/11
 */
@Repository
public class RoleDaoImpl extends SuperDao implements RoleDao {
    @Override
    public Role find(int id) throws AppException {
        return this.getSqlSession().selectOne("RoleMapper.find", id);
    }

    @Override
    public Role findByCode(String code) throws AppException {
        return this.getSqlSession().selectOne("RoleMapper.findByCode", code);
    }

    @Override
    public List<Role> findAll() throws AppException {
        SqlSession sqlSession = this.getSqlSession();
        List<Role> roles = sqlSession.selectList("RoleMapper.findAll");

        roles.forEach(role -> role.setPrivileges(sqlSession.selectList("PrivilegeMapper.findByRole", role.getId())));

        return roles;
    }

    @Override
    public List<Role> findPureAll() throws AppException {
        return this.getSqlSession().selectList("RoleMapper.findAll");
    }

    public List<Role> findByUser(int userId) throws AppException {
        SqlSession sqlSession = this.getSqlSession();
        List<Role> roles = sqlSession.selectList("RoleMapper.findByUser", userId);

        roles.forEach(role -> role.setPrivileges(sqlSession.selectList("PrivilegeMapper.findByRole", role.getId())));

        return roles;
    }

    @Override
    public List<Role> findPureByUser(int userId) throws AppException {
        return this.getSqlSession().selectList("RoleMapper.findByUser", userId);
    }

    @Override
    public List<Role> findByUser(String username) throws AppException {
        // roles.forEach(role -> role.setPrivileges(sqlSession.selectList("PrivilegeMapper.findByRole", role.getId())));
        return this.getSqlSession().selectList("RoleMapper.findByUsername", username);
    }
}
