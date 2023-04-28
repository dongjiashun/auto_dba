package com.autodb.ops.dms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

/**
 * spring security
 * @author dongjs
 * @since 2015/11/4
 */
@Configuration
public class SecurityConfig {
    @Bean
    public ExpressionUrlAuthorizationConfigurer<HttpSecurity> expressionUrlAuthorizationConfigurer() {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity> expressionUrlAuthorizationConfigurer = new ExpressionUrlAuthorizationConfigurer<>();

        // anyRequest -> authenticated
        expressionUrlAuthorizationConfigurer.getRegistry()
         		.antMatchers("/**").hasAuthority("ROLE_ANONYMOUS")
                .antMatchers("/task/all", "/task/all-data").hasAuthority("TASK_MANAGE")
                .antMatchers("/security/**").hasAuthority("SECURITY_MANAGE")
                .antMatchers("/datasource/**").hasAuthority("DATASOURCE_MANAGE")
                .antMatchers("/user/manage/**", "/user/role/**").hasAuthority("USER_MANAGE")
                .antMatchers("/system/**", "/manage", "/manage/**").hasAuthority("SYS_MANAGE")
                .anyRequest().authenticated();

        return expressionUrlAuthorizationConfigurer;
    }
}
