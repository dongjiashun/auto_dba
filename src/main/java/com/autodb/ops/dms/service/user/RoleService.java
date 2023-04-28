package com.autodb.ops.dms.service.user;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.user.Role;

import java.util.List;

/**
 * RoleService
 *
 * @author dongjs
 * @since 16/2/1
 */
public interface RoleService {
    List<Role> findAll() throws AppException;

    List<Role> findPureAll() throws AppException;

    List<Role> findPureByUser(int userId) throws AppException;
}
