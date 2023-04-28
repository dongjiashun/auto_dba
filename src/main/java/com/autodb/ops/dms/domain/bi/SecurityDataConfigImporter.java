package com.autodb.ops.dms.domain.bi;

//import com.autodb.cryption.CryptionClient;
//import com.autodb.cryption.SecrecyMeta;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.security.SecurityData;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.security.SecurityDataDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * import SecurityData config
 *
 * @author dongjs
 * @since 16/5/13
 */
@Component
@ConditionalOnProperty(prefix = "sec", name = "import")
public class SecurityDataConfigImporter {
    private static Logger log = LoggerFactory.getLogger(SecurityDataConfigImporter.class);

    @Value("${task.candidate.alternate}")
    private String user;

    @Value("${api.encryption.service}")
    private String endPoint;

    @Value("${api.encryption.token}")
    private String token;

    @Value("${api.encryption.secret}")
    private String secret;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private SecurityDataDao securityDataDao;

    // @Scheduled(initialDelay = 10000, fixedRate = 24 * 3600000)
    // @Scheduled(cron = "0 0 1 * * *")
    public void scheduledImportConfig() {
        importConfig();
    }

    public synchronized String importConfig() {
        /*String msg;
        CryptionClient client = null;
        try {
            long start = System.currentTimeMillis();
            client = new CryptionClient(user, endPoint, token, secret);
            List<SecrecyMeta> secrecyMetadata = client.getSecrecyMetas();
            long end = System.currentTimeMillis();
            log.info("CryptionClient getSecrecyMetas cost {} ms", end - start);

            msg = importMetadate(secrecyMetadata);
        } catch (Exception e) {
            msg = "CryptionClient getSecrecyMetas exception: " + e.getMessage();
            log.warn(msg, e);
        } finally {
            if (client != null) {
                try {
                    client.shutdown();
                } catch (Exception e) {
                    log.warn("shutdown CryptionClient exception: {}", e.getMessage());
                    // ignore
                }
            }
        }*/
        return "";
    }

    @Transactional
    public String importMetadate(List<Object> secrecyMetadata) {
       /* String msg;
        Map<String, DataSource> dataSourceMap = dataSourceDao.findByEnv(DataSource.Env.PROD).stream()
                .collect(Collectors.toMap(DataSource::getSid, dataSource -> dataSource, (sid1, sid2) -> sid2));
        List<SecurityData> securityData = secrecyMetadata.stream()
                .map(meta -> meta.getColumnConfigs().keySet().stream()
                        .map(column -> {
                            DataSource dataSource = dataSourceMap.get(meta.getDbName());
                            if (dataSource != null) {
                                SecurityData data = new SecurityData();
                                data.setDataSource(dataSource);
                                data.setTable(meta.getTableName().toUpperCase());
                                data.setColumn(column.toUpperCase());
                                data.setGmtCreate(new Date());
                                return data;
                            } else {
                                return null;
                            }
                        }).filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .reduce((list1, list2) -> {
                    list1.addAll(list2);
                    return list1;
                }).orElse(Collections.emptyList());

        if (securityData.size() > 0) {
            // delete
            securityDataDao.deleteAll();
            // add all
            securityDataDao.add(securityData);

            msg = "import security data config, count " + securityData.size() + ", at " + new Date();
            log.info(msg);
        } else {
            msg = "no config data available";
        }
*/
        return "";
    }
}
