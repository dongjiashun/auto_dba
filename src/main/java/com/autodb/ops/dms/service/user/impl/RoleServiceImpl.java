package com.autodb.ops.dms.service.user.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.user.Role;
import com.autodb.ops.dms.repository.user.RoleDao;
import com.autodb.ops.dms.service.user.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RoleService Impl
 *
 * @author dongjs
 * @since 16/2/1
 */
@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleDao roleDao;

    @Override
    public List<Role> findAll() throws AppException {
        return roleDao.findAll();
    }

    @Override
    public List<Role> findPureAll() throws AppException {
        return roleDao.findPureAll();
    }

    @Override
    public List<Role> findPureByUser(int userId) throws AppException {
        return roleDao.findPureByUser(userId);
    }
}
