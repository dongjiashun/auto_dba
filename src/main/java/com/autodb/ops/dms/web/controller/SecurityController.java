package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.dto.security.SecurityAuthQuery;
import com.autodb.ops.dms.entity.security.SecurityDataAuth;
import com.autodb.ops.dms.service.datasource.DataSourceAuthService;
import com.autodb.ops.dms.service.security.SecurityDataAuthService;
import com.autodb.ops.dms.service.security.SecurityDataService;
import com.dianwoba.springboot.webapi.WebApiResponse;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Security Controller
 *
 * @author dongjs
 * @since 16/1/28
 */
@Controller
@Validated
@RequestMapping("/security/")
public class SecurityController extends SuperController {
    @Autowired
    private SecurityDataService securityDataService;

    @Autowired
    private SecurityDataAuthService securityDataAuthService;

    @Autowired
    private DataSourceAuthService dataSourceAuthService;

    @RequestMapping(value = "data", method = RequestMethod.GET)
    public String dataPage() {
        return "security/data";
    }

    @RequestMapping(value = "data", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse data(@RequestParam("datasource") int datasource,
                               @RequestParam("table") @NotBlank String table,
                               @RequestParam(value = "columns[]", required = false) String[] columns) {
        securityDataService.update(datasource, table,
                columns != null && columns.length > 0 ? Arrays.asList(columns) : Collections.emptyList());
        return WebApiResponse.success(true);
    }

    @RequestMapping(value = "data-auth", method = RequestMethod.GET)
    public String dataAuthPage() {
        return "security/data_auth";
    }

    @RequestMapping(value = "data-auth", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse dataAuth(@RequestParam("data[]") @Size(min = 1) Integer[] data,
                           @RequestParam("users[]") @Size(min = 1) String[] users) {
        List<Integer> securityDataList = data != null && data.length > 0 ? Arrays.asList(data) : Collections.emptyList();
        List<String> userList = users != null && users.length > 0 ? Arrays.asList(users) : Collections.emptyList();
        securityDataAuthService.add(securityDataList, userList);
        return WebApiResponse.success(true);
    }

    @RequestMapping(value = "data-auth/detail", method = RequestMethod.GET)
    public String dataAuthDetailPage() {
        return "security/data_auth_detail";
    }

    @RequestMapping(value = "data-auth/detail_data", method = RequestMethod.GET)
    @ResponseBody
    public Page<SecurityDataAuth> dataAuthDetailData(HttpServletRequest request, SecurityAuthQuery query) {
        Page<SecurityDataAuth> page = this.getPage(request);
        securityDataAuthService.findByQuery(query, page);
        return page;
    }

    @RequestMapping(value = "data-auth/del", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse dataAuthDel(@RequestParam("ids[]") @Size(min = 1) Integer[] ids) {
        return WebApiResponse.success(securityDataAuthService.delete(Arrays.asList(ids)));
    }

    @RequestMapping(value = "/ds/{id}/{name}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse structInfo(@PathVariable("id") Integer id,
                                     @PathVariable("name") String name) {
        return WebApiResponse.success(securityDataService.tableInfo(id, name));
    }

    @RequestMapping(value = "/ds/{id}/table/list", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> securityTableList(@PathVariable("id") int id) {
        List<String> tableList = securityDataService.securityTableList(id);

        List<HashMap<String, Object>> userList = dataSourceAuthService.findByDs(id).stream()
                .map(auth -> new HashMap<String, Object>() {
                    {
                        put("id", auth.getUser().getId());
                        put("username", auth.getUser().getUsername());
                    }
                }).collect(Collectors.toList());

        return new HashMap<String, Object>() {
            {
                put("tables", tableList);
                put("users", userList);
            }
        };
    }

    @RequestMapping(value = "/ds/{id}/sec/{name}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse securityTableStructInfo(@PathVariable("id") Integer id,
                                     @PathVariable("name") String name) {
        return WebApiResponse.success(securityDataService.securityTableInfo(id, name));
    }
}
