package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.AppContext;
import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.dto.ds.SimpleDataSource;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.user.RoleDao;
import com.autodb.ops.dms.service.datasource.DataSourceAuthService;
import com.autodb.ops.dms.service.datasource.DataSourceRoleService;
import com.autodb.ops.dms.service.datasource.DataSourceService;
import com.autodb.ops.dms.web.exception.PageNotFoundException;
import com.dianwoba.springboot.webapi.WebApiResponse;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DataSource manage controller
 * @author dongjs
 * @since 2015/12/29
 */
@Controller
@Validated
@RequestMapping("/datasource")
public class DataSourceManageController extends SuperController {
    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DataSourceRoleService dataSourceRoleService;

    @Autowired
    private DataSourceAuthService dataSourceAuthService;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String manage() {
        return "datasource/manage";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public List<DataSource> listDataSource(@RequestParam(value = "env", required = false) String env) {

        return dataSourceService.findByEnv(DataSource.Env.getEnv(env));
    }

    @RequestMapping(value = "/list2", method = RequestMethod.GET)
    @ResponseBody
    public List<SimpleDataSource> listSimpleDataSource(@RequestParam(value = "env", required = false) String env) {
        List<DataSource> dataSources = dataSourceService.findByEnv(DataSource.Env.getEnv(env));
        return SimpleDataSource.of(dataSources);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DataSource dataSource(@PathVariable("id") Integer id) {
        DataSource dataSource = dataSourceService.find(id);
        if (dataSource != null) {
            DataSourceEncryptUtils.decryptPassword(dataSource);
        }
        return dataSource;
    }

    @RequestMapping(value = "/{id}/auth", method = RequestMethod.GET)
    public String dataSourceAuthPage(@PathVariable("id") Integer id,  Model model) {
        DataSource datasource = dataSourceService.find(id);
        if (datasource == null) {
            // 404
            throw new PageNotFoundException();
        }
        List<DataSourceRole> roles = dataSourceRoleService.findAll();
        model.addAttribute("datasource", datasource);
        model.addAttribute("roles", roles);
        return "datasource/auth";
    }

    @RequestMapping(value = "/{id}/auth", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse dataSourceAuth(@PathVariable("id") Integer id,
                                         @RequestParam("users[]") @Size(min = 1) Integer[] users,
                                         @RequestParam(value = "roles[]", required = false) String[] roles) {
        DataSource dataSource = dataSourceService.find(id);
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

    @RequestMapping(value = "/{id}/{type}", method = RequestMethod.GET)
    @ResponseBody
    public List<String> structNames(@PathVariable("id") Integer id, @PathVariable("type") String type) {
        return dataSourceService.structNames(id, type);
    }

    @RequestMapping(value = "/{id}/{type}/{name}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse structInfo(@PathVariable("id") Integer id,
                                     @PathVariable("type") String type,
                                     @PathVariable("name") String name) {
        return WebApiResponse.success(dataSourceService.structInfo(id, type, name));
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse testConnection(@Valid DataSource dataSource) {
        return WebApiResponse.success(dataSourceService.testConnection(dataSource));
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse add(@Valid DataSource dataSource) {
        return WebApiResponse.success(dataSourceService.add(dataSource));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse update(@Valid DataSource dataSource) {
        return WebApiResponse.success(dataSourceService.update(dataSource));
    }

    @RequestMapping(value = "/{id}/del", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse delete(@PathVariable("id") Integer id) {
        return WebApiResponse.success(dataSourceService.delete(id));
    }

    @RequestMapping(value = "/auth/del", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse deleteAuth(@RequestBody @Size(min = 1) Integer[] ids) {
        return WebApiResponse.success(dataSourceAuthService.delete(Arrays.asList(ids)));
    }

    @RequestMapping(value = "/proxy/test", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse testProxyConnection(@Valid DataSource dataSource) {
        return WebApiResponse.success(dataSourceService.testProxyConnection(dataSource));
    }

    @RequestMapping(value = "/{id}/sct/{table}", method = RequestMethod.GET)
    @ResponseBody
    public WebApiResponse showCreateTable(@PathVariable("id") Integer id, @PathVariable("table") String table) {
        return WebApiResponse.success(dataSourceService.showCreateTable(id, table));
    }

    //新需求，默认管理拥有所有的datasource的所有权限
    @RequestMapping(value = "/authtoall", method = RequestMethod.GET)
    @ResponseBody
    public WebApiResponse autoToAll() {
        User user = AppContext.getCurrentUser();
        List<com.autodb.ops.dms.entity.user.Role> roles = roleDao.findByUser(user.getUsername());
        boolean addAllDataSourceToAdmin = false;
        for(com.autodb.ops.dms.entity.user.Role role : roles){
            if(role.getCode().equals("admin")){
                addAllDataSourceToAdmin = true;
                break;
            }
        }
        if(!addAllDataSourceToAdmin)
            return WebApiResponse.success("access deny!");
        List<String> roleNames = ImmutableList.of("dev","exporter","reviewer","owner");
        List<Integer> userIds = Arrays.asList(user.getId());
        List<DataSource> dataSourceList = dataSourceDao.findAll();
        for(DataSource dataSource : dataSourceList){
            dataSourceAuthService.add(dataSource, userIds, roleNames);
        }
        return WebApiResponse.success("success auth!");
    }
}
