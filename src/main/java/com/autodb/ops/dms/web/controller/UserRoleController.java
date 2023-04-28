package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.entity.user.Role;
import com.autodb.ops.dms.service.user.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * RoleController
 *
 * @author dongjs
 * @since 16/1/30
 */
@Controller
@Validated
@RequestMapping("/user/role")
public class UserRoleController extends SuperController {
    @Autowired
    private RoleService roleService;

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String manage() {
        return "user/role/manage";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public List<Role> list() {
        return roleService.findAll();
    }
}
