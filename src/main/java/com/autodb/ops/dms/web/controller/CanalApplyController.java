package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.dto.task.CanalApplyAuditForm;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.service.task.CanalApplyService;
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

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * CanalApply Controller
 *
 * @author dongjs
 * @since 2016/11/1
 */
@Controller
@Validated
public class CanalApplyController extends SuperController {
    @Autowired
    private CanalApplyService canalApplyService;

    @RequestMapping(value = "/task/canal-apply", method = RequestMethod.GET)
    public String start() {
        return "task/canal_apply/start";
    }

    /**
     * apply
     **/
    @RequestMapping(value = "/task/canal-apply/apply", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse apply(@RequestParam("env") @NotBlank String env,
                                @RequestParam("ds") @Min(1) Integer ds,
                                @RequestParam("table") @NotBlank String table,
                                @RequestParam("reason") @NotBlank @Size(max = 1000) String reason) throws Exception {
        return WebApiResponse.success(canalApplyService.apply(getUser(), env, ds, table, reason));
    }

    /** audit **/
    @RequestMapping(value = "/task/canal-apply/audit/{id}", method = RequestMethod.GET)
    public String auditPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = canalApplyService.task("audit", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        model.addAttribute("managers", canalApplyService.managers(taskData.getTaskBiz()));
        return "task/canal_apply/audit";
    }

    @RequestMapping(value = "/task/canal-apply/audit/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse audit(@PathVariable("id") String id, @Valid CanalApplyAuditForm auditForm) {
        return WebApiResponse.success(canalApplyService.approve(id, getUser(), auditForm));
    }

    /** adjust **/
    @RequestMapping(value = "/task/canal-apply/adjust/{id}", method = RequestMethod.GET)
    public String adjustPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = canalApplyService.task("adjust", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/canal_apply/adjust";
    }

    @RequestMapping(value = "/task/canal-apply/adjust/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse adjust(@PathVariable("id") String id,
                                 @RequestParam("apply") @NotNull Boolean apply,
                                 @RequestParam("reason") @NotBlank @Size(max = 1000) String reason) {
        return WebApiResponse.success(canalApplyService.adjust(this.getUser(), id, apply, reason));
    }

    /** process **/
    @RequestMapping(value = "/process/canal-apply/{id}", method = RequestMethod.GET)
    public String processDetail(@PathVariable("id") String id, Model model) {
        ProcessData process = canalApplyService.process(id);
        if (process == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("process", process);
        return "task/canal_apply/process";
    }
}
