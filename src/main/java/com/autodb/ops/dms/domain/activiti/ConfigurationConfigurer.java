package com.autodb.ops.dms.domain.activiti;

import com.autodb.ops.dms.domain.activiti.identity.GroupManagerFactory;
import com.autodb.ops.dms.domain.activiti.identity.UserManagerFactory;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * ProcessEngineConfiguration Configurer
 *
 * @author dongjs
 * @since 16/1/13
 */
@Component
public class ConfigurationConfigurer implements ProcessEngineConfigurationConfigurer {
    @Autowired
    private UserManagerFactory userManagerFactory;

    @Autowired
    private GroupManagerFactory groupManagerFactory;

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setCustomSessionFactories(Arrays.asList(userManagerFactory, groupManagerFactory));
    }
}
