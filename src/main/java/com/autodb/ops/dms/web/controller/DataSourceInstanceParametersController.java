package com.autodb.ops.dms.web.controller;

import com.aliyuncs.rds.model.v20140815.DescribeParametersResponse;
import com.dianwoba.springboot.webapi.WebApiResponse;
import com.autodb.ops.dms.common.JSON;
import com.autodb.ops.dms.domain.aliyun.RdsService;
import com.autodb.ops.dms.entity.datasource.InstanceParameters;
import com.autodb.ops.dms.service.datasource.InstanceParametersService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@Validated
@RequestMapping("/datasource/parameters")
public class DataSourceInstanceParametersController {
    @Autowired
    private InstanceParametersService instanceParametersService;

    @Autowired
    private RdsService rdsService;

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String manage() {
        return "datasource/parameters";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public List<InstanceParameters> listDataSource(@RequestParam(value = "env", required = false) String env) {
        List<InstanceParameters> instanceParametersList = instanceParametersService.findAll();
        for(InstanceParameters instanceParameters : instanceParametersList){
            instanceParameters.convertParametersToMap();
        }

        return instanceParametersList;
//        return departmentService.findByEnv(DataSource.Env.getEnv(env));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public InstanceParameters dataSource(@PathVariable("id") Integer id) {
        InstanceParameters instanceParameters = instanceParametersService.find(id);
        /*if (department != null) {
            DataSourceEncryptUtils.decryptPassword(dataSource);
        }*/
        return instanceParameters;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse add(@Valid InstanceParameters instanceParameters) {
        return WebApiResponse.success(instanceParametersService.add(instanceParameters));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse update(@Valid InstanceParameters instanceParameters) {
        return WebApiResponse.success(instanceParametersService.update(instanceParameters));
    }

    @RequestMapping(value = "/{id}/del", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse delete(@PathVariable("id") Integer id) {
        return WebApiResponse.success(instanceParametersService.delete(id));
    }

    @RequestMapping(value = "/{id}/active", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse active(@PathVariable("id") Integer id) {
        InstanceParameters instanceParameters = instanceParametersService.find(id);
        String instanceId = instanceParameters.getDbinstance().split("\\.")[0];
        Map<String,String> paramsMap = JSON.parseObject(instanceParameters.getParameters(),Map.class);
        return rdsService.active(instanceId,paramsMap);
    }

    @RequestMapping(value = "/{id}/states", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse states(@PathVariable("id") Integer id) {
        InstanceParameters instanceParameters = instanceParametersService.find(id);
        String instanceId = instanceParameters.getDbinstance().split("\\.")[0];
        return rdsService.activeResult(instanceId);
    }
}
