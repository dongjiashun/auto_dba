package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.dto.user.SimpleUser;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

/**
 * UserController
 *
 * @author dongjs
 * @since 16/1/30
 */
@Controller
@Validated
@RequestMapping("/user")
public class UserController extends SuperController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public List<SimpleUser> userQuery(@RequestParam(value = "q", required = false) String query) {
        if (StringUtils.isBlank(query) || query.length() == 1) {
            return Collections.emptyList();
        }

        List<User> users = userService.findLikeUsername(query);
        return SimpleUser.of(users);
    }
}
