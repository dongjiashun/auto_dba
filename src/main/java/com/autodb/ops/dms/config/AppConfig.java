package com.autodb.ops.dms.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.autodb.ops.dms.domain.feign.SelfIdentityDecoder;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.autodb.ops.dms.common.util.OkHttpUtils;
import com.autodb.ops.dms.domain.canal.CanalService;
import com.autodb.ops.dms.domain.dingding.DingdingService;
import com.autodb.ops.dms.domain.feign.WebApiJacksonDecoder;
import com.autodb.ops.dms.domain.inception.InceptionService;
import com.autodb.ops.dms.domain.staff.StaffService;
import com.autodb.ops.dms.domain.strongbox.StrongboxService;
//import com.autodb.pt.druid.masking.process.filter.WacDruidLogFilter;
import feign.Request;
import feign.Target;
import feign.hystrix.HystrixFeign;
import feign.hystrix.SetterFactory;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * application config
 * @author dongjs
 * @since 2015/11/9
 */
@Configuration
@EnableScheduling
public class AppConfig {
    @Bean(name = "emailSenderExecutorService")
    public ExecutorService emailSenderExecutorService() {
        return emailSenderExecutorFactoryBean().getObject();
    }

    @Bean
    public ThreadPoolExecutorFactoryBean emailSenderExecutorFactoryBean() {
        ThreadPoolExecutorFactoryBean executorFactoryBean = new ThreadPoolExecutorFactoryBean();
        executorFactoryBean.setThreadNamePrefix("email_sender_thread_pool");
        executorFactoryBean.setKeepAliveSeconds(1800);
        executorFactoryBean.setCorePoolSize(1);
        executorFactoryBean.setQueueCapacity(10);
        executorFactoryBean.setMaxPoolSize(10);
        return executorFactoryBean;
    }

    @Bean
    public ServletRegistrationBean hystrixMetricsStreamServlet() {
        ServletRegistrationBean servlet = new ServletRegistrationBean();
        servlet.setServlet(new HystrixMetricsStreamServlet());
        servlet.addUrlMappings("/hystrix.stream");
        return servlet;
    }

    @Configuration
    public static class ApiConfig {
        @Value("${api.staff.service}")
        private String staffApiService;

        @Value("${api.strongbox.service}")
        private String strongboxApiService;

        @Value("${api.strongbox.test.service}")
        private String strongboxApiTestService;

        @Value("${api.inception.service}")
        private String inceptionApiService;

        @Value("${api.inception.test.service}")
        private String inceptionApiTestService;

        @Value("${api.dingding.service}")
        private String dingdingApiService;

        @Value("${api.canal.service}")
        private String canalApiService;

        private final int inceptionTimeout = 200*1000;//200*1000;
        @Bean
        public StaffService staffService() {
            return HystrixFeign.builder().setterFactory(new SetterFactory() {
                        @Override
                        public HystrixCommand.Setter create(Target<?> target, Method method) {
                            return HystrixCommand.Setter
                                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey(StaffService.class.getSimpleName()))// 控制 StaffService 下,所有方法的Hystrix Configuration
                                    .andCommandPropertiesDefaults(
                                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(10000) // 超时配置
                                    );
                        }
                    })
                    .client(new OkHttpClient())
                    .decoder(new WebApiJacksonDecoder())
                    .target(StaffService.class, staffApiService);
        }

        @Bean
        @Primary
        public StrongboxService strongboxService() {
            return HystrixFeign.builder()
                    .client(new OkHttpClient())
                    .encoder(new JacksonEncoder())
                    .target(StrongboxService.class, strongboxApiService);
        }

        @Bean
        public StrongboxService strongboxTestService() {
            return HystrixFeign.builder()
                    .client(new OkHttpClient())
                    .encoder(new JacksonEncoder())
                    .target(StrongboxService.class, strongboxApiTestService);
        }

        @Bean
        @Primary
        public InceptionService inceptionService() {
            return HystrixFeign.builder().setterFactory(new SetterFactory() {
                @Override
                public HystrixCommand.Setter create(Target<?> target, Method method) {
                    return HystrixCommand.Setter
                            .withGroupKey(HystrixCommandGroupKey.Factory.asKey(InceptionService.class.getSimpleName()))// 控制 DingdingService 下,所有方法的Hystrix Configuration
                            .andCommandPropertiesDefaults(
                                    HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(inceptionTimeout)
                            );
                }
            })
            .client(new OkHttpClient())
            .options(new Request.Options(inceptionTimeout/2,inceptionTimeout))
            .decoder(new SelfIdentityDecoder())
            .target(InceptionService.class, inceptionApiService);
        }

        @Bean
        public InceptionService inceptionTestService() {
            return HystrixFeign.builder().setterFactory(new SetterFactory() {
                @Override
                public HystrixCommand.Setter create(Target<?> target, Method method) {
                    return HystrixCommand.Setter
                            .withGroupKey(HystrixCommandGroupKey.Factory.asKey(InceptionService.class.getSimpleName()))// 控制 DingdingService 下,所有方法的Hystrix Configuration
                            .andCommandPropertiesDefaults(
                                    HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(inceptionTimeout)
                            );
                }
            })
                    .client(new OkHttpClient())
                    .options(new Request.Options(inceptionTimeout/2,inceptionTimeout))
                    .decoder(new SelfIdentityDecoder())
                    .target(InceptionService.class, inceptionApiTestService);
        }

        @Bean
        Request.Options feignOptions() {
            return new Request.Options(100 * 1000, 100 * 1000);
        }


        @Bean
        public DingdingService dingdingService(){
            return HystrixFeign.builder().setterFactory(new SetterFactory() {
                @Override
                public HystrixCommand.Setter create(Target<?> target, Method method) {
                    return HystrixCommand.Setter
                            .withGroupKey(HystrixCommandGroupKey.Factory.asKey(DingdingService.class.getSimpleName()))// 控制 DingdingService 下,所有方法的Hystrix Configuration
                            .andCommandPropertiesDefaults(
                                    HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(10000) // 超时配置
                            );
                }
            })
            .client(new OkHttpClient())
            .decoder(new WebApiJacksonDecoder())
            .target(DingdingService.class,dingdingApiService);
        }

        @Bean
        public CanalService canalService() {
            return HystrixFeign.builder()
                    .client(new OkHttpClient())
                    .decoder(new JacksonDecoder())
                    .target(CanalService.class, canalApiService);
        }
    }

    @Configuration
    public static class DataSourceConfig implements EnvironmentAware {
        private RelaxedPropertyResolver resolver;

        @Override
        public void setEnvironment(Environment environment) {
            this.resolver = new RelaxedPropertyResolver(environment, "mysql.");
        }

        @Bean(name = "dataSource",
                initMethod = "init", destroyMethod = "close")
        public DruidDataSource dataSource() {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName(resolver.getProperty("driverClassName"));
            dataSource.setUrl(resolver.getProperty("url"));
            dataSource.setUsername(resolver.getProperty("username"));
            dataSource.setPassword(resolver.getProperty("password"));

            dataSource.setInitialSize(resolver.getProperty("initialSize", Integer.class));
            dataSource.setMinIdle(resolver.getProperty("minIdle", Integer.class));
            dataSource.setMaxActive(resolver.getProperty("maxActive", Integer.class));
            dataSource.setMaxWait(resolver.getProperty("maxWait", Long.class));

            dataSource.setTimeBetweenConnectErrorMillis(120000);
            dataSource.setMinEvictableIdleTimeMillis(600000);
            dataSource.setValidationQuery("select 1");
            dataSource.setPoolPreparedStatements(true);
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);

            dataSource.setProxyFilters(Arrays.asList(statFilter()));//fixme Arrays.asList(statFilter(), null)
            return dataSource;
        }

        @Bean
        public StatFilter statFilter() {
            StatFilter statFilter = new StatFilter();
            statFilter.setMergeSql(true);
            return statFilter;
        }

       /* @Bean
        public WacDruidLogFilter wacDruidLogFilter() {
            WacDruidLogFilter wacFilter = new WacDruidLogFilter();
            wacFilter.setLogEnable(false);
            return wacFilter;
        }*/

        @Bean
        public DataSourceTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        public SqlSessionFactory sqlSessionFactory(ApplicationContext context) throws Exception {
            SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
            sessionFactory.setDataSource(dataSource());
            sessionFactory.setTypeAliasesPackage("com.autodb.ops.dms.entity");
            sessionFactory.setMapperLocations(context.getResources("classpath:mybatis/mapper/**/*.xml"));
            sessionFactory.setConfigLocation(context.getResource("classpath:mybatis/mybatis-config.xml"));
            return sessionFactory.getObject();
        }
    }
}
