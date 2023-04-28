package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.domain.bi.SecurityDataConfigImporter;
import com.autodb.ops.dms.domain.strongbox.StrongboxService;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceProxy;
import com.autodb.ops.dms.service.datasource.DataSourceProxyService;
import com.autodb.ops.dms.service.datasource.DataSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * system
 *
 * @author dongjs
 * @since 15/10/23
 */
@Controller
@Validated
@RequestMapping("/system/tool")
public class SystemToolController extends SuperController {
    private static Logger logger = LoggerFactory.getLogger(SystemToolController.class);

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DataSourceProxyService dataSourceProxyService;

    @Autowired
    private StrongboxService strongboxService;

    @Autowired
    @Qualifier("strongboxTestService")
    private StrongboxService strongboxTestService;

    @Autowired(required = false)
    private SecurityDataConfigImporter securityDataConfigImporter;

    @RequestMapping(value = "strongbox/prod/export", method = RequestMethod.GET)
    @ResponseBody
    public String strongboxProdExport() {
        List<DataSource> dataSources = dataSourceService.findByEnv(DataSource.Env.PROD);
        return strongboxExport(DataSource.Env.PROD, dataSources);
    }

    @RequestMapping(value = "strongbox/test/export", method = RequestMethod.GET)
    @ResponseBody
    public String strongboxTestExport() {
        List<DataSource> dataSources = dataSourceService.findByEnv(DataSource.Env.TEST);
        return strongboxExport(DataSource.Env.TEST, dataSources);
    }

    private String strongboxExport(String env, List<DataSource> dataSources) {
        String result = "success import failed, no datasource available";
        if (dataSources.size() > 0) {
            Map<Integer, DataSourceProxy> proxyMap = dataSourceProxyService.findAll().stream()
                    .collect(Collectors.toMap(DataSourceProxy::getId, proxy -> proxy));
            dataSources.forEach(ds -> {
                DataSourceProxy proxy = ds.getProxy();
                if (proxy != null && proxy.getId() > 0) {
                    ds.setProxy(proxyMap.get(proxy.getId()));
                }
            });

            if (DataSource.Env.PROD.equals(env)) {
                strongboxService.importDataSources(StrongboxService.JdbcPropertiesSet.of(dataSources));
                logger.info("import {} datasources into strongbox prod env", dataSources.size());
                result = "success import " + dataSources.size() + " datasources into strongbox prod env";
            } else if (DataSource.Env.TEST.equals(env)) {
                strongboxTestService.importDataSources(StrongboxService.JdbcPropertiesSet.of(dataSources));
                logger.info("import {} datasources into strongbox test env", dataSources.size());
                result = "success import " + dataSources.size() + " datasources into strongbox test env";
            } else {
                result = "success import failed, unknown env";
            }
        }
        return result;
    }

    @RequestMapping(value = "security/import", method = RequestMethod.GET)
    @ResponseBody
    public String securityDataImport() {
        if (securityDataConfigImporter != null) {
            return securityDataConfigImporter.importConfig();
        } else {
            return "securityDataConfigImporter is null";
        }
    }
}
