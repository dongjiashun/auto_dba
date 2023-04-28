package com.autodb.ops.dms.domain.canal;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.domain.datasource.observer.DataSourceChange;
import com.autodb.ops.dms.domain.datasource.observer.DataSourceObserver;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceCobar;
import com.autodb.ops.dms.repository.datasource.DataSourceCobarDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Canal DataSourceObserver
 *
 * @author dongjs
 * @since 2016/10/31
 */
@Component
public class CanalDataSourceObserver extends DataSourceObserver {
    @Autowired
    private CanalService canalService;

    @Autowired
    private DataSourceCobarDao dataSourceCobarDao;

    @Override
    public void update(DataSourceChange change) {
        DataSource dataSource = change.getDataSource();
        switch (change.getChange()) {
            case ADD:
            case UPDATE:
                importDataSource(dataSource);
                break;
            case DELETE:
                deleteDataSource(dataSource);
                break;
        }
    }

    @Override
    protected boolean ignoreException() {
        return true;
    }

    private void importDataSource(DataSource dataSource) {
        getIpPorts(dataSource)
                .forEach(pair -> canalService.importDatasource(pair.getLeft(), pair.getRight()));
    }

    private void deleteDataSource(DataSource dataSource) {
        getIpPorts(dataSource)
                .forEach(pair -> canalService.deleteDatasource(pair.getLeft(), pair.getRight()));
    }

    private List<Pair<String, Integer>> getIpPorts(DataSource ds) {
        List<Pair<String, Integer>> pairs = Collections.emptyList();
        if (!ds.isCobar()) {
            pairs = Collections.singletonList(Pair.of(ds.getHost2(), ds.getPort2()));
        } else {
            DataSourceCobar cobar = dataSourceCobarDao.findByDataSource(ds);
            if (cobar != null) {
                pairs = cobar.shardings()
                        .stream()
                        .map(sharding -> Pair.of(sharding.getSlaveHost(), sharding.getSlavePort()))
                        .collect(Collectors.toList());
            }
        }
        return pairs;
    }
}
