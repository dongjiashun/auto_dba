package com.autodb.ops.dms.domain.strongbox;

import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceProxy;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Strongbox Service
 * @author dongjs
 * @since 16/4/21
 */
public interface StrongboxService {
    @RequestLine("GET /key/{project}")
    String key(@Param("project") String projectId);

    @RequestLine("POST /import")
    @Headers("Content-Type: application/json")
    void importDataSources(JdbcPropertiesSet jdbcPropertiesSet);

    @RequestLine("DELETE /remove/{database}")
    void removeDataSource(@Param("database") String name);

    /** JdbcProperties **/
    @Data
    class JdbcProperties {
        private final String name;
        private final String address;
        private final String username;
        private final String password;

        public JdbcProperties(String name, String address, String username, String password) {
            this.name = name;
            this.address = address;
            this.username = username;
            this.password = password;
        }
    }

    /** JdbcPropertiesSet **/
    @Data
    class JdbcPropertiesSet {
        private static final String SLAVE_PREFIX = "s:";

        private final Set<JdbcProperties> set;

        public JdbcPropertiesSet(Set<JdbcProperties> set) {
            this.set = set;
        }

        public static Set<String> databases(DataSource dataSource) {
            Set<String> databases = new HashSet<>();
            if (dataSource.getProxy() != null) {
                databases.add(dataSource.getProxySid());
            } else {
                databases.add(dataSource.getSid());
            }
            databases.add(SLAVE_PREFIX + dataSource.getSid());
            return databases;
        }

        public static JdbcPropertiesSet of(DataSource dataSource) {
            return new JdbcPropertiesSet(dataSource2JdbcProperties(dataSource));
        }

        public static JdbcPropertiesSet of(List<DataSource> dataSources) {
            return new JdbcPropertiesSet(dataSources.stream()
                    .map(JdbcPropertiesSet::dataSource2JdbcProperties)
                    .reduce((set1, set2) -> {
                        set1.addAll(set2);
                        return set1;
                    })
                    .orElse(Collections.emptySet()));
        }

        private static Set<JdbcProperties> dataSource2JdbcProperties(DataSource dataSource) {
            Set<JdbcProperties> jdbcPropertiesSet = new HashSet<>();

            // proxy or master
            if (validProxy(dataSource)) {
                jdbcPropertiesSet.add(new JdbcProperties(dataSource.getProxySid(),
                        dataSource.getProxy().getHost() + ':' + dataSource.getProxyPort(),
                        dataSource.getProxyUsername(), DataSourceEncryptUtils.getDecryptProxyPassword(dataSource)));
                // slave
                jdbcPropertiesSet.add(new JdbcProperties(SLAVE_PREFIX + dataSource.getSid(), dataSource.getHost2() + ':' + dataSource.getPort2(),
                        dataSource.getProxyUsername(), DataSourceEncryptUtils.getDecryptProxyPassword(dataSource)));
            } else {
                jdbcPropertiesSet.add(new JdbcProperties(dataSource.getSid(), dataSource.getHost() + ':' + dataSource.getPort(),
                        dataSource.getUsername(), DataSourceEncryptUtils.getDecryptPassword1(dataSource)));
                // slave
                jdbcPropertiesSet.add(new JdbcProperties(SLAVE_PREFIX + dataSource.getSid(), dataSource.getHost2() + ':' + dataSource.getPort2(),
                        dataSource.getUsername2(), DataSourceEncryptUtils.getDecryptPassword2(dataSource)));

            }

            return jdbcPropertiesSet;
        }

        private static boolean validProxy(DataSource dataSource) {
            DataSourceProxy proxy = dataSource.getProxy();
            return proxy != null && StringUtils.isNotEmpty(proxy.getHost());
        }
    }
}
