package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.dto.ds.UserDataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.entity.user.Role;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.service.datasource.DataSourceAuthService;
import com.autodb.ops.dms.service.user.RoleService;
import com.autodb.ops.dms.service.user.UserService;
import com.dianwoba.springboot.webapi.WebApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UserController
 *
 * @author dongjs
 * @since 16/1/30
 */
@Controller
@Validated
@RequestMapping("/user/manage")
public class UserManageController extends SuperController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DataSourceAuthService dataSourceAuthService;

    @RequestMapping(method = RequestMethod.GET)
    public String manage() {
        return "user/manage";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Page<User> selectHistory(HttpServletRequest request,
                                    @RequestParam(value = "q", required = false) String query) {
        Page<User> page = this.getPage(request);
        userService.find(query, page);
        return page;
    }

    @RequestMapping(value = "{id}/auth_data", method = RequestMethod.GET)
    @ResponseBody
    public List<UserDataSource> authData(@PathVariable("id") @NotNull Integer userId) {
        List<DataSourceAuth> authList = dataSourceAuthService.findByUser(userId);
        return UserDataSource.of(authList);
    }

    @RequestMapping(value = "{id}/role_data", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> roleData(@PathVariable("id") @NotNull Integer userId) {
        List<Role> roles = roleService.findPureAll();
        List<Role> userRoles = roleService.findPureByUser(userId);
        return new HashMap<String, Object>() {
            {
                put("roles", roles);
                put("userRoles", userRoles.stream().map(Role::getId).collect(Collectors.toList()));
            }
        };
    }

    @RequestMapping(value = "{id}/roles", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse userRoles(@PathVariable("id") @NotNull Integer userId,
                                    @RequestParam(value = "roles[]", required = false) Integer[] roles) {
        List<Integer> roleIds = roles != null ? Arrays.asList(roles) : Collections.emptyList();
        this.userService.updateRoles(userId, roleIds);
        return WebApiResponse.success(true);
    }
}
