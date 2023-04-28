package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.dto.task.DataChangeApply;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.service.task.DataChangeService;
import com.autodb.ops.dms.web.exception.PageNotFoundException;
import com.dianwoba.springboot.webapi.WebApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
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

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DataChange Controller
 *
 * @author dongjs
 * @since 16/1/25
 */
@Controller
@Validated
public class DataChangeController extends SuperController {
    @Autowired
    private DataChangeService dataChangeService;

    /**
     * start
     **/
    @RequestMapping(value = "/task/data-change", method = RequestMethod.GET)
    public String start() {
        return "task/data_change/start";
    }

    @RequestMapping(value = "/task/data-change/apply", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse apply(@RequestBody @Valid DataChangeApply dataChangeApply) throws Exception {
        validateDataChangeApply(dataChangeApply);
        return WebApiResponse.success(dataChangeService.apply(this.getUser(), dataChangeApply));
    }

    /** audit **/
    @RequestMapping(value = "/task/data-change/audit/{id}", method = RequestMethod.GET)
    public String auditPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = dataChangeService.task("audit", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/data_change/audit";
    }

    @RequestMapping(value = "/task/data-change/audit/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse audit(@PathVariable("id") String id,
                                @RequestParam("agree") @NotNull Boolean agree,
                                @RequestParam("backup") @NotNull Boolean backup,
                                @RequestParam("reason") @NotBlank @Size(max = 255) String reason) {
        return WebApiResponse.success(dataChangeService.approve(id, this.getUser(), agree, backup, reason));
    }

    /** adjust **/
    @RequestMapping(value = "/task/data-change/adjust/{id}", method = RequestMethod.GET)
    public String adjustPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = dataChangeService.task("adjust", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/data_change/adjust";
    }

    @RequestMapping(value = "/task/data-change/adjust/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse adjust(@PathVariable("id") String id,
                                 @RequestParam("apply") @NotNull Boolean apply,
                                 @RequestParam("sql") @NotBlank @Size(max = Constants.SQL_MAX_SIZE) String sql,
                                 @RequestParam("reason") @NotBlank @Size(max = 1000) String reason) {
        return WebApiResponse.success(dataChangeService.adjust(this.getUser(), id, apply, reason, sql));
    }

    /** view result **/
    @RequestMapping(value = "/task/data-change/result/{id}", method = RequestMethod.GET)
    public String resultPage(@PathVariable("id") String id, Model model) {
        ProcessData process = dataChangeService.processByTaskId("result", id);
        if (process == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("taskId", id);
        model.addAttribute("process", process);
        return "task/data_change/result";
    }

    @RequestMapping(value = "/task/data-change/result/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse downloadData(@PathVariable("id") String id) {
        return WebApiResponse.success(dataChangeService.result(this.getUser(), id));
    }

    /** process **/
    @RequestMapping(value = "/process/data-change/{id}", method = RequestMethod.GET)
    public String processDetail(@PathVariable("id") String id, Model model) {
        ProcessData process = dataChangeService.process(id);
        model.addAttribute("process", process);
        if (process == null) {
            // 404
            throw new PageNotFoundException();
        }
        return "task/data_change/process";
    }

    private void validateDataChangeApply(DataChangeApply dataChangeApply) throws ValidationException {
        dataChangeApply.getChanges().forEach(change -> {
            Integer ds = change.getDs();
            String sql = change.getSql();
            if (ds == null || ds < 1 || StringUtils.isBlank(sql) || sql.length() > Constants.SQL_MAX_SIZE) {
                throw new ValidationException("Invalid parameter data changes ds or sql");
            }
        });
    }
}
