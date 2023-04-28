package com.autodb.ops.dms.repository.user;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.user.UserRole;

import java.util.List;

public interface UserRoleDao {
    List<UserRole> findAll() throws AppException;
}
