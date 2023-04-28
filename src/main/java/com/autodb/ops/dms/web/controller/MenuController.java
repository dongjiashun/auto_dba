package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.service.sys.MenuService;
import com.dianwoba.springboot.webapi.WebApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Menu Controller
 * @author dongjs
 * @since 16/3/29
 */
@Controller
@RequestMapping("/system/menu")
@Validated
public class MenuController extends SuperController {
    @Autowired
    private MenuService menuService;

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String manage(Model model) {
        model.addAttribute("menusConfig", menuService.menusConfig());
        return "system/menu";
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public WebApiResponse update(@RequestParam("menus") String menus) {
        return WebApiResponse.success(menuService.update(menus));
    }
}
