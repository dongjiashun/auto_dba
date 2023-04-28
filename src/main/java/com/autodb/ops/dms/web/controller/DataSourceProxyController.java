package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.entity.datasource.DataSourceProxy;
import com.autodb.ops.dms.service.datasource.DataSourceProxyService;
import com.dianwoba.springboot.webapi.WebApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DataSourceProxy Controller
 *
 * @author dongjs
 * @since 16/4/20
 */
@Controller
@Validated
@RequestMapping("/datasource/proxy")
public class DataSourceProxyController {
    @Autowired
    private DataSourceProxyService dataSourceProxyService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public List<DataSourceProxy> dataSourceProxies() {
        return dataSourceProxyService.findAll();
    }

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String manage(Model model) {
        List<DataSourceProxy> proxies = dataSourceProxyService.findAll();
        model.addAttribute("proxies", proxies);
        return "datasource/proxy/manage";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DataSourceProxy dataSourceProxy(@PathVariable("id") @NotNull Integer id) {
        return dataSourceProxyService.find(id);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse add(@Valid DataSourceProxy dataSourceProxy) {
        return WebApiResponse.success(dataSourceProxyService.add(dataSourceProxy));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse update(@Valid DataSourceProxy dataSourceProxy) {
        return WebApiResponse.success(dataSourceProxyService.update(dataSourceProxy));
    }

    @RequestMapping(value = "/{id}/del", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse delete(@PathVariable("id") Integer id) {
        return WebApiResponse.success(dataSourceProxyService.delete(id));
    }
}
