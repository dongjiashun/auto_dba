package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.ProcessDataQuery;
import com.autodb.ops.dms.service.task.TaskService;
import com.dianwoba.springboot.webapi.WebApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Task Controller
 *
 * @author dongjs
 * @since 16/1/15
 */
@Controller
@Validated
@RequestMapping("/task/")
public class TaskController extends SuperController {
    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "pending", method = RequestMethod.GET)
    @ResponseBody
    public WebApiResponse pending() {
        return WebApiResponse.success(taskService.userTasks(this.getUser()));
    }

    @RequestMapping(value = "/pending/count", method = RequestMethod.GET)
    @ResponseBody
    public WebApiResponse auditCount() {
        return WebApiResponse.success(taskService.userTasksCount(this.getUser()));
    }

    @RequestMapping(value = "my", method = RequestMethod.GET)
    @ResponseBody
    public Page<ProcessData> my(HttpServletRequest request) {
        Page<ProcessData> page = this.getPage(request);
        taskService.userProcesses(this.getUser(), page);
        return page;
    }

    @RequestMapping(value = "related", method = RequestMethod.GET)
    public String relatedPage() {
        return "task/related";
    }

    @RequestMapping(value = "related-data", method = RequestMethod.GET)
    @ResponseBody
    public Page<ProcessData> related(HttpServletRequest request, @Valid ProcessDataQuery processDataQuery) {
        Page<ProcessData> page = this.getPage(request);
        taskService.userRelatedProcesses(this.getUser(), processDataQuery, page);
        return page;
    }

    @RequestMapping(value = "all", method = RequestMethod.GET)
    public String allPage() {
        return "task/all";
    }

    @RequestMapping(value = "all-data", method = RequestMethod.GET)
    @ResponseBody
    public Page<ProcessData> all(HttpServletRequest request, @Valid ProcessDataQuery processDataQuery) {
        Page<ProcessData> page = this.getPage(request);
        taskService.allProcesses(processDataQuery, page);
        return page;
    }

    @RequestMapping(value = "cancel/{processInstanceId}", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse<Integer> cancel(@PathVariable("processInstanceId") String processInstanceId) {
        return WebApiResponse.success(taskService.cancel(this.getUser().getUsername(), processInstanceId));
    }
}
