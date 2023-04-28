package com.autodb.ops.dms.repository.user.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.user.UserRole;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.user.UserRoleDao;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRoleDaoImpl extends SuperDao implements UserRoleDao {
    @Override
    public List<UserRole> findAll() throws AppException {
        SqlSession sqlSession = this.getSqlSession();
        List<UserRole> userRoles = sqlSession.selectList("UserRoleMapper.findAll");
        return userRoles;
    }
}
