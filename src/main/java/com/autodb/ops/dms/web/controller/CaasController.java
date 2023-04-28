package com.autodb.ops.dms.web.controller;

import com.dianwoba.springboot.webapi.WebApiResponse;
import com.autodb.ops.dms.dto.caas.ChangeStash;
import com.autodb.ops.dms.dto.ds.UserDataSource;
import com.autodb.ops.dms.dto.task.StructStashOnline;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.entity.task.StructChangeStash;
import com.autodb.ops.dms.service.datasource.DataSourceAuthService;
import com.autodb.ops.dms.service.task.StructChangeService;
//import com.autodb.springboot.webapi.WebApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Caas Controller
 * @author dongjs
 * @since 2016/12/29
 */
@Controller
@Validated
@RequestMapping("/caas")
public class CaasController extends SuperController {
    @Autowired
    private DataSourceAuthService dataSourceAuthService;

    @Autowired
    private StructChangeService structChangeService;

    @RequestMapping(value = "/stash", method = RequestMethod.GET)
    public String stash(Model model) {
        List<DataSourceAuth> authList = dataSourceAuthService.findByUserEnv(this.getUser().getId(), DataSource.Env.TEST);
        model.addAttribute("dataSources", UserDataSource.of(authList));
        return "caas/stash";
    }

    @RequestMapping(value = "/stash", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse stash(@Valid ChangeStash stash) {
        return WebApiResponse.success(structChangeService.stash(this.getUser(), stash));
    }

    @RequestMapping(value = "/online", method = RequestMethod.GET)
    public String online(Model model) {
        List<DataSourceAuth> authList = dataSourceAuthService.findByUserEnv(this.getUser().getId(), DataSource.Env.TEST);
        model.addAttribute("dataSources", UserDataSource.of(authList));
        return "caas/online";
    }

    @RequestMapping(value = "/online/{id}", method = RequestMethod.GET)
    @ResponseBody
    public List<StructChangeStash> onlineStash(@PathVariable("id") @NotNull Integer id) {
        return structChangeService.onlineStash(id);
    }

    @RequestMapping(value = "/online/{id}/{task}", method = RequestMethod.GET)
    @ResponseBody
    public StructStashOnline onlineStashData(@PathVariable("id") @NotNull Integer id,
                                             @PathVariable("task") @NotNull Integer task) {
        return StructStashOnline.of(id, task, structChangeService.onlineStash(id));
    }

    @RequestMapping(value = "/online", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse online(@RequestBody @Valid StructStashOnline online) {
        return WebApiResponse.success(structChangeService.online(this.getUser(), online));
    }

}
