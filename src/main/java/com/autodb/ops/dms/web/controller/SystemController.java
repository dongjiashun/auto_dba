package com.autodb.ops.dms.web.controller;

import com.google.common.base.Joiner;
import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.dto.user.OperateLogQuery;
import com.autodb.ops.dms.entity.user.OperateLog;
import com.autodb.ops.dms.service.user.OperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * system
 *
 * @author dongjs
 * @since 15/10/23
 */
@Controller
@Validated
@RequestMapping("/system")
public class SystemController extends SuperController {
    @Autowired
    private Environment env;

    @Autowired
    private OperateLogService operateLogService;

    @RequestMapping(value = "config", method = RequestMethod.GET)
    public String index(Model model) throws IOException {
        Map<String, Object> properties = new HashMap<>();
        for (PropertySource<?> propertySource : ((AbstractEnvironment) env).getPropertySources()) {
            if (propertySource instanceof PropertiesPropertySource) {
                properties.putAll(((PropertiesPropertySource) propertySource).getSource());
                break;
            }
        }
        List<String> propList = properties.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> entry.getKey() + '=' + entry.getValue())
                .collect(Collectors.toList());

        model.addAttribute("config", Joiner.on('\n').join(propList));
        return "system/config";
    }

    @RequestMapping(value = "op-log", method = RequestMethod.GET)
    public String operateLogPage() throws IOException {
        return "system/op_log";
    }

    @RequestMapping(value = "op-log-data", method = RequestMethod.GET)
    @ResponseBody
    public Page<OperateLog> operateLog(HttpServletRequest request, OperateLogQuery query) {
        Page<OperateLog> page = this.getPage(request);
        operateLogService.query(query, page);
        return page;
    }
}
