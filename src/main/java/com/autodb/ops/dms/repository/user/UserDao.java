package com.autodb.ops.dms.repository.user;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.user.LdapUser;
import com.autodb.ops.dms.entity.user.User;

import java.util.List;
import java.util.Map;

/**
 * user dao
 * @author dongjs
 * @since 2015/11/9
 */
public interface UserDao {
//	判断用户是否过期
	User useroverdue (String username) throws AppException;
    User find(int id) throws AppException;
    User findByUsername(String username) throws AppException;
    User findByEmail(String email) throws AppException;
    List<User> findByRole(String code) throws AppException;

    List<User> findByUsernames(List<String> usernames) throws AppException;

    List<User> findLikeUsername(String username) throws AppException;

    List<User> findAll(Page<User> page) throws AppException;

    List<User> findLikeAll(String username, Page<User> page) throws AppException;

    /** id -> user **/
    Map<Integer, User> findMap(List<Integer> ids) throws AppException;

    void add(User user) throws AppException;

    void updateRoles(User user, List<Integer> roles) throws AppException;

    void injectRoles(User user) throws AppException;
    
    boolean updateLdapInfo(LdapUser user) throws AppException;
	User findByNickname(String nickname) throws AppException;
}
