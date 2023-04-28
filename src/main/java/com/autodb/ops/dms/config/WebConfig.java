package com.autodb.ops.dms.config;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.autodb.ops.dms.web.interceptor.ApiTokenInterceptor;
import com.autodb.ops.dms.web.interceptor.DmsWebContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * custom web mvc config
 *
 * @author dongjs
 * @since 15/10/9
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private DmsWebContextInterceptor dmsWebContextInterceptor;

    @Autowired
    private ApiTokenInterceptor apiTokenInterceptor;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }

    /** <mvc:default-servlet-handler /> */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /** JSR-349 */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	//入口url验证
        registry.addInterceptor(dmsWebContextInterceptor).addPathPatterns("/**");
        registry.addInterceptor(apiTokenInterceptor).addPathPatterns("/api/**");
    }

    /**
     * druid stat config
     */
    @Configuration
    public static class DruidConfig {
        @Bean
        public ServletRegistrationBean statViewServlet() {
            ServletRegistrationBean statViewServlet = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
            statViewServlet.setName("DruidStatView");

            Map<String, String> initParams = new HashMap<>();
            initParams.put("loginUsername", "dongjiashun");
            initParams.put("loginPassword", "1212ming");
            statViewServlet.setInitParameters(initParams);
            return statViewServlet;
        }

        @Bean
        public FilterRegistrationBean webStatFilter() {
            FilterRegistrationBean webStatFilter = new FilterRegistrationBean();
            webStatFilter.setName("DruidWebStatFilter");
            webStatFilter.setFilter(new WebStatFilter());
            webStatFilter.setUrlPatterns(Collections.singletonList("/*"));

            Map<String, String> initParams = new HashMap<>();
            initParams.put("exclusions", "/static/*,*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
            webStatFilter.setInitParameters(initParams);
            return webStatFilter;
        }
    }
}