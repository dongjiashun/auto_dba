package com.autodb.ops.dms.domain.activiti.identity;

import com.autodb.ops.dms.entity.user.Role;
import com.autodb.ops.dms.repository.user.RoleDao;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GroupEntityManager
 *
 * @author dongjs
 * @since 16/1/13
 */
@Component
public class GroupEntityManager extends
        org.activiti.engine.impl.persistence.entity.GroupEntityManager {

    @Autowired
    private RoleDao roleDao;

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
        List<Group> groups;
        if (StringUtils.isNotEmpty(query.getId())) {
            Role role = roleDao.findByCode(query.getId());
            groups = role != null ? Collections.singletonList(of(role)) : Collections.emptyList();
        } else if (StringUtils.isNotEmpty(query.getUserId())) {
            groups = this.findGroupsByUser(query.getUserId());
        } else {
            groups = super.findGroupByQueryCriteria(query, page);
        }

        return groups;
    }

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        long count;
        if (StringUtils.isNotEmpty(query.getId())) {
            Role role = roleDao.findByCode(query.getId());
            count = role != null ? 1 : 0;
        } else if (StringUtils.isNotEmpty(query.getUserId())) {
            count = this.findGroupsByUser(query.getUserId()).size();
        } else {
            count = super.findGroupCountByQueryCriteria(query);
        }

        return count;
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        return of(roleDao.findByUser(userId));
    }

    /**
     * role -> group
     **/
    public static Group of(Role role) {
        GroupEntity groupEntity = new GroupEntity(role.getCode());
        groupEntity.setName(role.getName());
        return groupEntity;
    }

    /**
     * roles -> groups
     **/
    public static List<Group> of(List<Role> roles) {
        return roles.stream().map(GroupEntityManager::of).collect(Collectors.toList());
    }
}
