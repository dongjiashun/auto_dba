package com.autodb.ops.dms.domain.activiti.identity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * GroupManager SessionFactory
 *
 * @author dongjs
 * @since 16/1/13
 */
@Component
public class GroupManagerFactory implements SessionFactory {
    @Autowired
    private GroupEntityManager groupEntityManager;

    @Override
    public Class<?> getSessionType() {
        return GroupIdentityManager.class;
    }

    @Override
    public Session openSession() {
        return groupEntityManager;
    }
}
