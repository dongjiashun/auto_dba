package com.autodb.ops.dms.web.controller;

import com.dianwoba.springboot.webapi.WebApiResponse;
import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.entity.datasource.Department;
import com.autodb.ops.dms.service.datasource.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@Validated
@RequestMapping("/datasource/department")
public class DataSourceDepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String manage() {
        return "datasource/department";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public List<Department> listDataSource(@RequestParam(value = "env", required = false) String env) {
        return departmentService.findAll();
//        return departmentService.findByEnv(DataSource.Env.getEnv(env));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Department dataSource(@PathVariable("id") Integer id) {
        Department department = departmentService.find(id);
        /*if (department != null) {
            DataSourceEncryptUtils.decryptPassword(dataSource);
        }*/
        return department;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse add(@Valid Department department) {
        return WebApiResponse.success(departmentService.add(department));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse update(@Valid Department department) {
        return WebApiResponse.success(departmentService.update(department));
    }

    @RequestMapping(value = "/{id}/del", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse delete(@PathVariable("id") Integer id) {
        return WebApiResponse.success(departmentService.delete(id));
    }
}
