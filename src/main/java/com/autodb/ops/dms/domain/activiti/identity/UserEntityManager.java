package com.autodb.ops.dms.domain.activiti.identity;

import com.autodb.ops.dms.repository.user.RoleDao;
import com.autodb.ops.dms.repository.user.UserDao;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserEntityManager
 *
 * @author dongjs
 * @since 16/1/13
 */
@Component
public class UserEntityManager extends
        org.activiti.engine.impl.persistence.entity.UserEntityManager {
    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Override
    public User findUserById(String userId) {
        com.autodb.ops.dms.entity.user.User user = userDao.findByUsername(userId);
        return of(user);
    }

    @Override
    public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page) {
        List<User> users;
        if (StringUtils.isNotEmpty(query.getGroupId())) {
            users = of(userDao.findByRole(query.getGroupId()));
        } else if (StringUtils.isNotEmpty(query.getId())) {
            com.autodb.ops.dms.entity.user.User user = userDao.findByUsername(query.getId());
            users = user != null ? Collections.singletonList(of(user)) : Collections.emptyList();
        } else if (StringUtils.isNotEmpty(query.getEmail())) {
            com.autodb.ops.dms.entity.user.User user = userDao.findByEmail(query.getEmail());
            users = user != null ? Collections.singletonList(of(user)) : Collections.emptyList();
        } else {
            users = super.findUserByQueryCriteria(query, page);
        }

        return users;
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl query) {
        long count;
        if (StringUtils.isNotEmpty(query.getGroupId())) {
            count = userDao.findByRole(query.getGroupId()).size();
        } else if (StringUtils.isNotEmpty(query.getId())) {
            com.autodb.ops.dms.entity.user.User user = userDao.findByUsername(query.getId());
            count = user != null ? 1 : 0;
        } else if (StringUtils.isNotEmpty(query.getEmail())) {
            com.autodb.ops.dms.entity.user.User user = userDao.findByEmail(query.getEmail());
            count = user != null ? 1 : 0;
        } else {
            count = super.findUserCountByQueryCriteria(query);
        }

        return count;
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        return GroupEntityManager.of(roleDao.findByUser(userId));
    }

    /**
     * user -> activiti user
     **/
    public static User of(com.autodb.ops.dms.entity.user.User user) {
        UserEntity newUser = new UserEntity(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setFirstName(user.getUsername());
        newUser.setLastName(user.getNickname());
        return newUser;
    }

    /**
     * users -> activiti users
     **/
    public static List<User> of(List<com.autodb.ops.dms.entity.user.User> users) {
        return users.stream().map(UserEntityManager::of).collect(Collectors.toList());
    }
}
