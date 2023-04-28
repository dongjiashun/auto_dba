package com.autodb.ops.dms.web.controller;

import com.aliyuncs.rds.model.v20140815.DescribeParametersResponse;
import com.dianwoba.springboot.webapi.WebApiResponse;
import com.autodb.ops.dms.domain.aliyun.RdsService;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@Validated
@RequestMapping("/aliyun")
public class AliyunController {

    @Autowired
    private RdsService rdsService;

    @RequestMapping(value = "/active", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse active(@RequestParam("DBInstanceId") @NotBlank String DBInstanceId, @RequestBody Map<String,String> parametersMap) {
        return rdsService.active(DBInstanceId,parametersMap);
    }

    @RequestMapping(value = "/params", method = RequestMethod.GET)
    @ResponseBody
    public WebApiResponse params(@RequestParam("DBInstanceId") @NotBlank String DBInstanceId) {
        return rdsService.activeResult(DBInstanceId);
    }
}
