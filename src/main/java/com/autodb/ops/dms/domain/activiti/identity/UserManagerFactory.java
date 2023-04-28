package com.autodb.ops.dms.domain.activiti.identity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UserManager SessionFactory
 *
 * @author dongjs
 * @since 16/1/13
 */
@Component
public class UserManagerFactory implements SessionFactory {
    @Autowired
    private UserEntityManager userEntityManager;

    @Override
    public Class<?> getSessionType() {
        return UserIdentityManager.class;
    }

    @Override
    public Session openSession() {
        return userEntityManager;
    }
}
