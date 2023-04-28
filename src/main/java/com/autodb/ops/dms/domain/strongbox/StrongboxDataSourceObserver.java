package com.autodb.ops.dms.domain.strongbox;

import com.autodb.ops.dms.domain.datasource.observer.DataSourceChange;
import com.autodb.ops.dms.domain.datasource.observer.DataSourceObserver;
import com.autodb.ops.dms.entity.datasource.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strongbox DataSourceObserver
 *
 * @author dongjs
 * @since 16/4/22
 */
@Component
public class StrongboxDataSourceObserver extends DataSourceObserver {
    private static Logger logger = LoggerFactory.getLogger(StrongboxDataSourceObserver.class);

    @Autowired
    private StrongboxService strongboxService;

    @Autowired
    @Qualifier("strongboxTestService")
    private StrongboxService strongboxTestService;

    @Override
    public void update(DataSourceChange change) {
        DataSource dataSource = change.getDataSource();
        switch (change.getChange()) {
            case ADD:
            case UPDATE:
                StrongboxService.JdbcPropertiesSet jdbcPropertiesSet = StrongboxService
                        .JdbcPropertiesSet.of(dataSource);
                importDataSources(dataSource.getEnv(), jdbcPropertiesSet);
                break;
            case DELETE:
                Set<String> sids = StrongboxService.JdbcPropertiesSet.databases(dataSource);
                removeDataSource(dataSource.getEnv(), sids);
                break;
        }
    }

    @Override
    protected boolean ignoreException() {
        return true;
    }

    private void importDataSources(String env, StrongboxService.JdbcPropertiesSet jdbcPropertiesSet) {
        if (DataSource.Env.PROD.equals(env)) {
            strongboxService.importDataSources(jdbcPropertiesSet);
            if (logger.isInfoEnabled() && jdbcPropertiesSet.getSet().size() > 0) {
                logger.info("import prod strongbox datasource {}", jdbcPropertiesSet.getSet().stream()
                        .map(StrongboxService.JdbcProperties::getName).collect(Collectors.toSet()));
            }
        } else if (DataSource.Env.TEST.equals(env)) {
            strongboxTestService.importDataSources(jdbcPropertiesSet);
            if (logger.isInfoEnabled() && jdbcPropertiesSet.getSet().size() > 0) {
                logger.info("import test strongbox datasource {}", jdbcPropertiesSet.getSet().stream()
                        .map(StrongboxService.JdbcProperties::getName).collect(Collectors.toSet()));
            }
        }
    }

    private void removeDataSource(String env, Set<String> sids) {
        if (DataSource.Env.PROD.equals(env)) {
            sids.forEach(sid -> {
                strongboxService.removeDataSource(sid);
                logger.info("remove prod strongbox datasource {}", sid);
            });
        } else if (DataSource.Env.TEST.equals(env)) {
            sids.forEach(sid -> {
                strongboxTestService.removeDataSource(sid);
                logger.info("remove test strongbox datasource {}", sid);
            });
        }
    }
}
