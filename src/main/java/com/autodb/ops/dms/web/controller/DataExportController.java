package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.dto.task.DataExportApply;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.service.task.DataExportService;
import com.autodb.ops.dms.web.exception.PageNotFoundException;
import com.dianwoba.springboot.webapi.WebApiResponse;
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

/**
 * DataExport Controller
 *
 * @author dongjs
 * @since 16/1/21
 */
@Controller
@Validated
public class DataExportController extends SuperController {
    @Autowired
    private DataExportService dataExportService;

    /**
     * start
     **/
    @RequestMapping(value = "/task/data-export", method = RequestMethod.GET)
    public String start() {
        return "task/data_export/start";
    }

    @RequestMapping(value = "/task/data-export/apply", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse apply(@RequestBody @Valid DataExportApply dataExportApply) throws Exception {
        validateDataExportApply(dataExportApply);
        // always true
        dataExportApply.setSecurity(true);
        return WebApiResponse.success(dataExportService.apply(this.getUser(), dataExportApply));
    }

    /** audit **/
    @RequestMapping(value = "/task/data-export/audit/{id}", method = RequestMethod.GET)
    public String auditPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = dataExportService.task("audit", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/data_export/audit";
    }

    @RequestMapping(value = "/task/data-export/audit/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse audit(@PathVariable("id") String id,
                                @RequestParam("agree") @Min(0) @Max(2) int agree,
                                @RequestParam("reason") @NotBlank @Size(max = 255) String reason,
                                @RequestParam(value = "execTime", required = false)
                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date execTime) {
        return WebApiResponse.success(dataExportService.approve(id, this.getUser(), agree, reason, execTime));
    }

    /** adjust **/
    @RequestMapping(value = "/task/data-export/adjust/{id}", method = RequestMethod.GET)
    public String adjustPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = dataExportService.task("adjust", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/data_export/adjust";
    }

    @RequestMapping(value = "/task/data-export/adjust/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse adjust(@PathVariable("id") String id,
                                 @RequestParam("apply") @NotNull Boolean apply,
                                 @RequestParam("sql") @NotBlank @Size(max = Constants.SQL_MAX_SIZE) String sql,
                                 @RequestParam("reason") @NotBlank @Size(max = 1000) String reason) {
        return WebApiResponse.success(dataExportService.adjust(this.getUser(), id, apply, reason, sql));
    }

    /** waiting data **/
    @RequestMapping(value = "/task/data-export/waiting/{id}", method = RequestMethod.GET)
    public String waiting(@PathVariable("id") String id, Model model) {
        TaskData taskData = dataExportService.task("waiting", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/data_export/waiting";
    }

    /** download data **/
    @RequestMapping(value = "/task/data-export/downloadData/{id}", method = RequestMethod.GET)
    public String downloadDataPage(@PathVariable("id") String id, Model model) {
        ProcessData process = dataExportService.processByTaskId("downloadData", id);
        if (process == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("taskId", id);
        model.addAttribute("process", process);
        return "task/data_export/download_data";
    }

    @RequestMapping(value = "/task/data-export/downloadData/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse downloadData(@PathVariable("id") String id) {
        return WebApiResponse.success(dataExportService.downloadData(this.getUser(), id));
    }

    /** process **/
    @RequestMapping(value = "/process/data-export/{id}", method = RequestMethod.GET)
    public String processDetail(@PathVariable("id") String id, Model model) {
        ProcessData process = dataExportService.process(id);
        model.addAttribute("process", process);
        if (process == null) {
            // 404
            throw new PageNotFoundException();
        }
        return "task/data_export/process";
    }

    private void validateDataExportApply(DataExportApply dataExportApply) throws ValidationException {
        List<DataExportApply.Export> exports = dataExportApply.getExports();
        exports.forEach(export -> {
            Integer ds = export.getDs();
            String sql = export.getSql();
            if (ds == null || ds < 1 || StringUtils.isBlank(sql) || sql.length() > Constants.SQL_MAX_SIZE) {
                throw new ValidationException("Invalid parameter data exports ds or sql");
            }
        });
    }
}
