package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.entity.sql.SqlHistory;
import com.autodb.ops.dms.service.sql.SqlService;
import com.dianwoba.springboot.webapi.WebApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

/**
 * Web Sql Controller
 * @author dongjs
 * @since 2015/12/29
 */
@Controller
@Validated
@RequestMapping("/sql")
public class SqlController extends SuperController {
    @Autowired
    private SqlService sqlService;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public String query() {
        return "sql/query";
    }

    @RequestMapping(value = "/format", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse format(@RequestParam("sql") String sql, @RequestParam("type") String type) {
        return WebApiResponse.success(sqlService.formatSql(sql, type));
    }

    @RequestMapping(value = "/selects", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse selectStatements(@RequestParam("sql") String sql, @RequestParam("type") String type,
                                           @RequestParam(value = "query", required = false) boolean query) {
        return WebApiResponse.success(query ? sqlService.queryStatements(sql, type) : sqlService.selectStatements(sql, type));
    }

    @RequestMapping(value = "/select", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse query(@RequestParam("datasource") @NotNull @Min(1) Integer dsId,
                                @RequestParam("sql") @NotBlank @Size(max = Constants.SQL_MAX_SIZE) String sql,
                                @RequestParam(value = "record", required = false) String record,
                                HttpServletRequest request) {
        Page<Map<String, Object>> page = this.getPage(request);

        Triple<Integer,Integer, String> query = sqlService.query(this.getUser().getId(), dsId, sql,
                StringUtils.isNotBlank(record), page);

        Map<String, Object> result = new HashMap<>();
        result.put("code", query.getLeft());
        result.put("costTime",query.getMiddle());
        result.put("error", query.getRight());
        if (query.getLeft() == 0) {
            result.put("page", page);
        }
        return WebApiResponse.success(result);
    }

    @RequestMapping(value = "/explain", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse explain(@RequestParam("datasource") @NotNull @Min(1) Integer dsId,
                                  @RequestParam("sql") @NotBlank @Size(max = Constants.SQL_MAX_SIZE) String sql,
                                  HttpServletRequest request) {
        Page<Map<String, Object>> page = this.getPage(request);

        Pair<Integer, String> query = sqlService.explain(this.getUser().getId(), dsId, sql, page);

        Map<String, Object> result = new HashMap<>();
        result.put("code", query.getLeft());
        result.put("error", query.getRight());
        if (query.getLeft() == 0) {
            result.put("page", page);
        }
        return WebApiResponse.success(result);
    }

    @RequestMapping(value = "/select/history", method = RequestMethod.GET)
    @ResponseBody
    public Page<SqlHistory> selectHistory(HttpServletRequest request) {
        Page<SqlHistory> page = this.getPage(request);
        this.sqlService.sqlSelectHistory(this.getUser().getId(), page);
        return page;
    }
}
