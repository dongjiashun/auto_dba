package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;
import com.autodb.ops.dms.service.datasource.DataSourceRoleService;
import com.autodb.ops.dms.service.task.DsApplyService;
import com.autodb.ops.dms.web.exception.PageNotFoundException;
import com.dianwoba.springboot.webapi.WebApiResponse;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DataSourceApply Controller
 *
 * @author dongjs
 * @since 16/1/14
 */
@Controller
@Validated
public class DsApplyController extends SuperController {
    @Autowired
    private DsApplyService dsApplyService;

    @Autowired
    private DataSourceRoleService dataSourceRoleService;

    /**
     * start
     **/
    @RequestMapping(value = "/task/ds-apply", method = RequestMethod.GET)
    public String start() {
        return "task/ds_apply/start";
    }

    /**
     * apply
     **/
    @RequestMapping(value = "/task/ds-apply/apply", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse apply(@RequestParam("env") @NotBlank String env,
                                @RequestParam("ds[]") @Size(min = 1) Integer[] ds,
                                @RequestParam("reason") @NotBlank @Size(max = 1000) String reason) throws Exception {
        List<Integer> dsList = Arrays.asList(ds);
        return WebApiResponse.success(dsApplyService.apply(getUser(), env, dsList, reason));
    }

    /** audit **/
    @RequestMapping(value = "/task/ds-apply/audit/{id}", method = RequestMethod.GET)
    public String auditPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = dsApplyService.task("audit", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        List<DataSourceRole> roles = dataSourceRoleService.findAll();
        model.addAttribute("task", taskData);
        model.addAttribute("roles", roles);
        return "task/ds_apply/audit";
    }

    @RequestMapping(value = "/task/ds-apply/audit/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse audit(@PathVariable("id") String id,
                                @RequestParam("agree") @NotNull Boolean agree,
                                @RequestParam("reason") @NotBlank @Size(max = 255) String reason,
                                @RequestParam(value = "role[]", required = false) String[] roles) {
        List<String> roleList = (roles != null && roles.length > 0) ? Arrays.asList(roles) : Collections.emptyList();
        return WebApiResponse.success(dsApplyService.approve(id, getUser(), agree, reason, roleList));
    }

    /** adjust **/
    @RequestMapping(value = "/task/ds-apply/adjust/{id}", method = RequestMethod.GET)
    public String adjustPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = dsApplyService.task("adjust", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/ds_apply/adjust";
    }

    @RequestMapping(value = "/task/ds-apply/adjust/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse adjust(@PathVariable("id") String id,
                                 @RequestParam("apply") @NotNull Boolean apply,
                                 @RequestParam("reason") @NotBlank @Size(max = 1000) String reason) {
        return WebApiResponse.success(dsApplyService.adjust(this.getUser(), id, apply, reason));
    }

    /** process **/
    @RequestMapping(value = "/process/ds-apply/{id}", method = RequestMethod.GET)
    public String processDetail(@PathVariable("id") String id, Model model) {
        ProcessData process = dsApplyService.process(id);
        if (process == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("process", process);
        return "task/ds_apply/process";
    }
}
