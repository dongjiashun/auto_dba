package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.common.cache.LocalCache;
import com.autodb.ops.dms.dto.ds.SimpleDataSource;
import com.autodb.ops.dms.dto.ds.UserDataSource;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;
import com.autodb.ops.dms.service.datasource.DataSourceAuthService;
import com.autodb.ops.dms.service.datasource.DataSourceRoleService;
import com.autodb.ops.dms.service.datasource.DataSourceService;
import com.autodb.ops.dms.web.exception.PageNotFoundException;
import com.dianwoba.springboot.webapi.WebApiResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.Size;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * DataSource controller
 *
 * @author dongjs
 * @since 2015/12/29
 */
@Controller
@Validated
@RequestMapping("/ds")
public class DataSourceController extends SuperController {
    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DataSourceAuthService dataSourceAuthService;

    @Autowired
    private DataSourceRoleService dataSourceRoleService;

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String manage() {
        return "ds/manage";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public List<UserDataSource> listDataSource(@RequestParam(value = "env", required = false) String env) {
        List<DataSourceAuth> authList = dataSourceAuthService.findByUserEnv(this.getUser().getId(),
                DataSource.Env.getEnv(env));
        return UserDataSource.of(authList);
    }

    @RequestMapping(value = "/list2", method = RequestMethod.GET)
    @ResponseBody
    public List<UserDataSource> listDataSource2(@RequestParam(value = "env", required = false) String env) {
        String dsEnv = DataSource.Env.getEnv(env);
        List<DataSourceAuth> authList = dataSourceAuthService.findByUserEnv(this.getUser().getId(), dsEnv);

        /*if (DataSource.Env.PROD.equals(dsEnv)) {
            Set<String> testSids = dataSourceService.findByEnv(DataSource.Env.TEST)
                    .stream()
                    .map(DataSource::getSid)
                    .collect(Collectors.toSet());
            authList = authList.stream()
                    .filter(auth -> !testSids.contains(auth.getDataSource().getSid()))
                    .collect(Collectors.toList());
        }*/
        return UserDataSource.of(authList);
    }

    @RequestMapping(value = "/{id}/auth", method = RequestMethod.GET)
    public String dataSourceAuthPage(@PathVariable("id") Integer id, Model model) {
        DataSource datasource = null;
        boolean isOwner = dataSourceAuthService.hasRole(this.getUser().getId(), id, Constants.ROLE_OWNER);
        if (isOwner) {
            datasource = dataSourceService.find(id);
        }
        if (datasource == null) {
            // 404
            throw new PageNotFoundException();
        }
        List<DataSourceRole> roles = dataSourceRoleService.findAll();
        model.addAttribute("datasource", datasource);
        model.addAttribute("roles", roles);
        return "ds/auth";
    }

    @RequestMapping(value = "/{id}/auth", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse dataSourceAuth(@PathVariable("id") Integer id,
                                         @RequestParam("users[]") @Size(min = 1) Integer[] users,
                                         @RequestParam(value = "roles[]", required = false) String[] roles) {
        DataSource dataSource = null;
        boolean isOwner = dataSourceAuthService.hasRole(this.getUser().getId(), id, Constants.ROLE_OWNER);
        if (isOwner) {
            dataSource = dataSourceService.find(id);
        }
        if (dataSource != null) {
            List<Integer> userIds = Arrays.asList(users);
            List<String> roleNames = roles != null && roles.length > 0 ? Arrays.asList(roles) : Collections.emptyList();
            dataSourceAuthService.add(dataSource, userIds, roleNames);
        }
        return WebApiResponse.success(true);
    }

    @RequestMapping(value = "/{id}/auth_data", method = RequestMethod.GET)
    @ResponseBody
    public List<DataSourceAuth> listDataSourceAuth(@PathVariable("id") Integer id) {
        return dataSourceAuthService.findByDs(id);
    }

    @RequestMapping(value = "/{id}/auth/del/{authId}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse deleteAuth(@PathVariable("id") Integer id, @PathVariable("authId") Integer authId) {
        DataSourceAuth dataSourceAuth = dataSourceAuthService.find(authId);
        if (dataSourceAuth == null || !this.getUser().getId().equals(dataSourceAuth.getUser().getId())) {
            // 404
            throw new PageNotFoundException();
        }
        return WebApiResponse.success(dataSourceAuthService.delete(Collections.singletonList(authId)));
    }

    @RequestMapping(value = "/{id}/auth/del", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse deleteAuth(@PathVariable("id") Integer id, @RequestBody @Size(min = 1) Integer[] ids) {
        boolean isOwner = dataSourceAuthService.hasRole(this.getUser().getId(), id, Constants.ROLE_OWNER);
        if (!isOwner) {
            // 404
            throw new PageNotFoundException();
        }
        return WebApiResponse.success(dataSourceAuthService.delete(Arrays.asList(ids)));
    }

    @RequestMapping(value = "/list/unauth", method = RequestMethod.GET)
    @ResponseBody
    public List<SimpleDataSource> unAuthDataSource(@RequestParam(value = "env", required = false) String env) {
        List<DataSource> dataSources = dataSourceService.findUnAuthByUserEnv(this.getUser().getId(),
                DataSource.Env.getEnv(env));
        return SimpleDataSource.of(dataSources);
    }

    @RequestMapping(value = "/struct", method = RequestMethod.GET)
    public String struct() {
        return "ds/struct";
    }

    @RequestMapping(value = "/{id}/{type}", method = RequestMethod.GET)
    @ResponseBody
    public List<String> structNames(@PathVariable("id") Integer id, @PathVariable("type") String type) {
        String structInfoCacheKey = id + "_"+type+"_"+type;
        int userId = this.getUser().getId();
        List<String> tables = Lists.newArrayList();
        try {
            tables = (List<String> )LocalCache.get(structInfoCacheKey, new Callable() {
                @Override
                public Object call() throws Exception {
                    List<String> tmpTables = dataSourceService.structNames(userId, id, type);
                    //写到缓存里面
                    if(tmpTables != null){
                        LocalCache.put(structInfoCacheKey,tmpTables);
                    }
                    return tmpTables;
                }
            });
        }catch (ExecutionException exp){
            tables = dataSourceService.structNames(userId, id, type);//再查询一次
            if(tables.size() > 0){
                LocalCache.put(structInfoCacheKey,tables);
            }
        }
        return tables;
    }

    @RequestMapping(value = "/{id}/{type}/{name}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse structInfo(@PathVariable("id") Integer id,
                                     @PathVariable("type") String type,
                                     @PathVariable("name") String name) {
        int userId = this.getUser().getId();
        Map<String, Object> tmpStructInfo = dataSourceService.structInfo(userId, id, type, name);
        return WebApiResponse.success(tmpStructInfo);
    }

    @RequestMapping(value = "/all/{id}/tables/columns", method = RequestMethod.GET)
    @ResponseBody
    public WebApiResponse tablesColumns(@PathVariable("id") Integer id) {
        String tablesColumnsCacheKey = id + "_tables"+"_columns_all";
        int userId = this.getUser().getId();
        Map<String,List<String>> tablesColumnsResult = Maps.newHashMap();
        try {
            tablesColumnsResult = (Map<String,List<String>>)LocalCache.get(tablesColumnsCacheKey, new Callable() {
                @Override
                public Object call() throws Exception {
                    Map<String,List<String>> tablesColumns = Maps.newHashMap();
                    List<String> tmpTables = dataSourceService.structNames(userId, id, "table");
                    for(String table : tmpTables){
                        List<String> columnNames = Lists.newArrayList();
                        Map<String, Object> tmpStructInfo = dataSourceService.structInfo(userId, id, "table", table);
                        List<Map<String,Object>> columns = (List<Map<String,Object>> )tmpStructInfo.get("struct");
                        for(Map<String,Object>column : columns){
                            String columnName = (String)column.get("COLUMN_NAME");
                            columnNames.add(columnName);
                        }
                        tablesColumns.put(table,columnNames);
                    }

                    //写到缓存里面
                    if(tablesColumns != null && tablesColumns.size() > 0){
                        LocalCache.put(tablesColumnsCacheKey,tablesColumns);
                    }
                    return tablesColumns;
                }
            });
        }catch (ExecutionException exp){
            exp.printStackTrace();
        }
        return WebApiResponse.success(tablesColumnsResult);
    }

    @RequestMapping(value = "/{sid}/available", method = RequestMethod.GET)
    @ResponseBody
    public WebApiResponse checkAvailable(@PathVariable("sid") @Size(max = 100) String sid,
                                         @RequestParam(value = "env", required = false) String env) {
        return WebApiResponse.success(dataSourceService.findByEnvSid(DataSource.Env.getEnv(env), sid) == null);
    }
}
