package com.autodb.ops.dms.web.controller;

import com.google.common.collect.ImmutableMap;
import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.dto.ds.SimpleDataSource;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.service.datasource.DataSourceService;
import com.autodb.ops.dms.service.task.StructChangeService;
import com.autodb.ops.dms.web.exception.PageNotFoundException;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Api Controller
 *
 * @author dongjs
 * @since 2016/12/5
 */
@Controller
@Validated
@RequestMapping("/api")
public class ApiController extends SuperController {
    private final DataSourceService dataSourceService;

    private final StructChangeService structChangeService;

    @Autowired
    public ApiController(DataSourceService dataSourceService, StructChangeService structChangeService) {
        this.dataSourceService = dataSourceService;
        this.structChangeService = structChangeService;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public String test() {
        return "ok";
    }


    @RequestMapping(value = "/db/list", method = RequestMethod.GET)
    @ResponseBody
    public List<SimpleDataSource> dbList() {
        return SimpleDataSource.of(dataSourceService.findByEnv(DataSource.Env.TEST));
    }

    @RequestMapping(value = "/db/{sid}/schema", method = RequestMethod.GET)
    @ResponseBody
    public String dbSchema(@PathVariable("sid") @NotBlank String sid, HttpServletResponse response) {
        Optional<String> schemaSql = dataSourceService.schemaSql(DataSource.Env.TEST, sid);
        if (schemaSql.isPresent()) {
            response.setContentType("text/plain; charset=UTF-8");
            return schemaSql.get();
        } else {
            throw new PageNotFoundException();
        }
    }

    @RequestMapping(value = "/db/{sid}/metadata", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> dbMetadata(@PathVariable("sid") @NotBlank String sid) {
        Optional<String> schemaSql = dataSourceService.schemaSql(DataSource.Env.TEST, sid);
        if (schemaSql.isPresent()) {
            DataSource dataSource = dataSourceService.findByEnvSid(DataSource.Env.TEST, sid);
            DataSourceEncryptUtils.decryptPassword(dataSource);
            return ImmutableMap.of("db", dataSource, "schema", schemaSql.get());
        } else {
            throw new PageNotFoundException();
        }
    }

    @RequestMapping(value = "/db/{sid}/sqlcheck", method = RequestMethod.GET)
    @ResponseBody
    public String sqlcheck(@PathVariable("sid") @NotBlank String sid,
                                      @RequestParam("sql") @NotBlank String sql) {
        return structChangeService.check(sql, DataSource.Env.TEST, sid).orElse("");
    }
}
