package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.SchemaApplyAdjustForm;
import com.autodb.ops.dms.dto.task.SchemaApplyForm;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.service.sys.SysConfigService;
import com.autodb.ops.dms.service.task.SchemaApplyService;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * SchemaApply Controller
 *
 * @author dongjs
 * @since 16/7/22
 */
@Controller
@Validated
public class SchemaApplyController extends SuperController {
    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private SchemaApplyService schemaApplyService;

    /** start **/
    @RequestMapping(value = "/task/schema-apply", method = RequestMethod.GET)
    public String start(Model model) {
        List<String> products = sysConfigService.findListValue("sys.products");
        List<String> scenes = sysConfigService.findListValue("sys.scenes");
        model.addAttribute("products", products);
        model.addAttribute("scenes", scenes);
        return "task/schema_apply/start";
    }

    @RequestMapping(value = "/task/schema-apply/apply", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse apply(@Valid SchemaApplyForm schemaApplyForm) {
        return WebApiResponse.success(schemaApplyService.apply(this.getUser(), schemaApplyForm.toSchemaApply()));
    }

    /** audit **/
    @RequestMapping(value = "/task/schema-apply/audit/{id}", method = RequestMethod.GET)
    public String auditPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = schemaApplyService.task("audit", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/schema_apply/audit";
    }

    @RequestMapping(value = "/task/schema-apply/audit/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse audit(@PathVariable("id") String id,
                                @RequestParam("agree") @NotNull Byte agree,
                                @RequestParam("ds") @NotNull Integer datasourceId,
                                @RequestParam("dsName") @Size(max = 30) String dsName,
                                @RequestParam("reason") @NotBlank @Size(max = 255) String reason) {
        return WebApiResponse.success(schemaApplyService.approve(id, this.getUser(), agree, datasourceId, dsName, reason));
    }

    /** adjust **/
    @RequestMapping(value = "/task/schema-apply/adjust/{id}", method = RequestMethod.GET)
    public String adjustPage(@PathVariable("id") String id, Model model) {
        TaskData taskData = schemaApplyService.task("adjust", id);
        if (taskData == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("task", taskData);
        return "task/schema_apply/adjust";
    }

    @RequestMapping(value = "/task/schema-apply/adjust/{id}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse adjust(@PathVariable("id") String id,
                                 @Valid SchemaApplyAdjustForm schemaApplyAdjustForm) {
        return WebApiResponse.success(schemaApplyService.adjust(this.getUser(), id, schemaApplyAdjustForm));
    }

    /** process **/
    @RequestMapping(value = "/process/schema-apply/{id}", method = RequestMethod.GET)
    public String processDetail(@PathVariable("id") String id, Model model) {
        ProcessData process = schemaApplyService.process(id);
        if (process == null) {
            // 404
            throw new PageNotFoundException();
        }
        model.addAttribute("process", process);
        return "task/schema_apply/process";
    }
}
