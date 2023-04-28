package com.autodb.ops.dms.repository.user;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.user.Role;

import java.util.List;

/**
 * role dao
 * @author dongjs
 * @since 2015/11/11
 */
public interface RoleDao {
    Role find(int id) throws AppException;

    Role findByCode(String code) throws AppException;

    List<Role> findAll() throws AppException;

    List<Role> findPureAll() throws AppException;

    List<Role> findByUser(int userId) throws AppException;

    List<Role> findPureByUser(int userId) throws AppException;

    List<Role> findByUser(String username) throws AppException;
}
