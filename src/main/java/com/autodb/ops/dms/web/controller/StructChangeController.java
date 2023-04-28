package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.SimpleStructChange;
import com.autodb.ops.dms.dto.task.StructChangeApply;
import com.autodb.ops.dms.dto.task.StructChangeOnline;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.service.datasource.DataSourceAuthService;
import com.autodb.ops.dms.service.task.StructChangeService;
import com.autodb.ops.dms.web.exception.PageNotFoundException;
import com.dianwoba.springboot.webapi.WebApiResponse;
import org.activiti.engine.ManagementService;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StructChange Controller
 *
 * @author dongjs
 * @since 16/5/26
 */
@Controller
@Validated
public class StructChangeController extends SuperController {
    private final StructChangeService structChangeService;

    private final DataSourceAuthService dataSourceAuthService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    public StructChangeController(StructChangeService structChangeService, DataSourceAuthService dataSourceAuthService) {
        this.structChangeService = structChangeService;
        this.dataSourceAuthService = dataSourceAuthService;
    }

    /**
     * start
     **/
    @RequestMapping(value = "/task/struct-change", method = RequestMethod.GET)
    public String start() {
        return "task/struct_change/start";
    }

    @RequestMapping(value = "/task/struct-change/apply", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse apply(@RequestBody @Valid StructChangeApply structChangeApply) throws Exception {
        validateDataChangeApply(structChangeApply);
        return WebApiResponse.success(structChangeService.apply(this.getUser(), structChangeApply));
    }

    /** audit **/
    @RequestMapping(value = "/task/struct-change/audit/{id}", method = RequestMethod.GET)
    public String auditPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = structChangeService.task("audit", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/struct_change/audit";
    }

    /*@RequestMapping(value = "/task/struct-change/audit/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse audit(@PathVariable("id") String id,
                                @RequestParam("agree") @NotNull Boolean agree,
                                @RequestParam("reason") @NotBlank @Size(max = 255) String reason) {
        return WebApiResponse.success(structChangeService.approve(id, this.getUser(), agree, reason));
    }*/

    @RequestMapping(value = "/task/struct-change/audit/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse audit(@PathVariable("id") String id,
                                @RequestParam("agree")  @Min(0) @Max(2) int agree,
                                @RequestParam("reason") @NotBlank @Size(max = 255) String reason,
                                @RequestParam(value = "execTime", required = false)
                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date execTime) {
        return WebApiResponse.success(structChangeService.approve(id, this.getUser(), agree, reason,execTime));
    }

    /** adjust **/
    @RequestMapping(value = "/task/struct-change/adjust/{id}", method = RequestMethod.GET)
    public String adjustPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = structChangeService.task("adjust", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/struct_change/adjust";
    }

    @RequestMapping(value = "/task/struct-change/adjust/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse adjust(@PathVariable("id") String id,
                                 @RequestParam("apply") @NotNull Boolean apply,
                                 @RequestParam("sql") @NotBlank @Size(max = Constants.SQL_MAX_SIZE) String sql,
                                 @RequestParam("reason") @NotBlank @Size(max = 1000) String reason) {
        return WebApiResponse.success(structChangeService.adjust(this.getUser(), id, apply, reason, sql));
    }

    /** waiting data **/
    @RequestMapping(value = "/task/struct-change/waiting/{id}", method = RequestMethod.GET)
    public String waiting(@PathVariable("id") String id, Model model) {
        TaskData taskData = structChangeService.task("waiting", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/struct_change/waiting";
    }

    /** change execute data **/
    @RequestMapping(value = "/task/struct-change/waiting/changeExecuteTime/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse waitingChangeExecuteTime(@PathVariable("id") String id,
                                           @RequestParam(value = "execTime", required = false)
                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date execTime) {
        return WebApiResponse.success(structChangeService.change_exec_time("waiting", id,execTime));
    }

    /** execute **/
    @RequestMapping(value = "/task/struct-change/execute/{id}", method = RequestMethod.GET)
    public String executePage(@PathVariable("id") String id, Model model) {
        TaskData taskData = structChangeService.task("execute", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/struct_change/execute";
    }

    @RequestMapping(value = "/task/struct-change/execute/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse execute(@PathVariable("id") String id,
                                @RequestParam("agree") @NotNull Byte agree,
                                @RequestParam("reason") @NotBlank @Size(max = 255) String reason) {
        return WebApiResponse.success(structChangeService.execute(id, this.getUser(), agree, reason));
    }

    @RequestMapping(value = "/task/struct-change/progress/{id}", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public WebApiResponse progress(@PathVariable("id") String id) {
        return WebApiResponse.success(structChangeService.progress(id));
    }

    @RequestMapping(value = "/task/struct-change/cancel-progress/{id}", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public WebApiResponse cancelProgress(@PathVariable("id") String id) {
        return WebApiResponse.success(structChangeService.cancelProgress(id));
    }

    /** view result **/
    @RequestMapping(value = "/task/struct-change/result/{id}", method = RequestMethod.GET)
    public String resultPage(@PathVariable("id") String id, Model model) {
        ProcessData process = structChangeService.processByTaskId("result", id);
        if (process == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("taskId", id);
        model.addAttribute("process", process);
        return "task/struct_change/result";
    }

    @RequestMapping(value = "/task/struct-change/result/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse result(@PathVariable("id") String id) {
        return WebApiResponse.success(structChangeService.result(this.getUser(), id));
    }

    /** process **/
    @RequestMapping(value = "/process/struct-change/{id}", method = RequestMethod.GET)
    public String processDetail(@PathVariable("id") String id, Model model) {
        ProcessData process = structChangeService.process(id);
        model.addAttribute("process", process);
        if (process == null) {
            // 404
            throw new PageNotFoundException();
        }
        return "task/struct_change/process";
    }


    @RequestMapping(value = "/task/struct-change/online", method = RequestMethod.GET)
    public String online(Model model) {
        List<DataSourceAuth> auths = dataSourceAuthService.findByUserEnv(this.getUser().getId(), DataSource.Env.PROD);
        model.addAttribute("auths", auths);
        return "task/struct_change/online";
    }

    @RequestMapping(value = "/task/struct-change/online/{id}", method = RequestMethod.GET)
    @ResponseBody
    public List<SimpleStructChange> onlineChanges(@PathVariable("id") @NotNull Integer id) {
        return structChangeService.onlineChanges(id, Integer.MAX_VALUE)
                .stream()
                .map(SimpleStructChange::of)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/task/struct-change/online-process/{id}", method = RequestMethod.GET)
    @ResponseBody
    public List<TaskBiz> onlineProcess(@PathVariable("id") @NotNull Integer id) {
        return structChangeService.inProcessOnline(id);
    }


    @RequestMapping(value = "/task/struct-change/online/{ds}/{task}", method = RequestMethod.GET)
    @ResponseBody
    public StructChangeOnline onlineData(@PathVariable("ds") @NotNull Integer ds,
                                         @PathVariable("task") @NotNull Integer task) {
        return StructChangeOnline.of(ds, task, structChangeService.onlineChanges(ds, task));
    }

    @RequestMapping(value = "/task/struct-change/online", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse online(@RequestBody @Valid StructChangeOnline online) {
        return WebApiResponse.success(structChangeService.online(this.getUser(), online));
    }

    private void validateDataChangeApply(StructChangeApply structChangeApply) throws ValidationException {
        structChangeApply.getChanges().forEach(change -> {
            Integer ds = change.getDs();
            String sql = change.getSql();
            if (ds == null || ds < 1 || StringUtils.isBlank(sql) || sql.length() > Constants.SQL_MAX_SIZE) {
                throw new ValidationException("Invalid parameter struct changes ds or sql");
            }
        });
    }
}
