package com.autodb.ops.dms.service.user;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.user.User;

import java.util.List;

/**
 * user service
 * @author dongjs
 * @since 2015/11/10
 */
public interface UserService {
    User findByUsername(String username) throws AppException;

    List<User> findLikeUsername(String username) throws AppException;

    List<User> find(String query, Page<User> page) throws AppException;

    User findOrAdd(String userCode) throws AppException;

    void updateRoles(int userId, List<Integer> roles) throws AppException;
}
