package com.autodb.ops.dms.dto.user;

import com.autodb.ops.dms.entity.user.User;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple User
 *
 * @author dongjs
 * @since 16/1/30
 */
@Data
public class SimpleUser {
    private int id;
    private String username;
    private String nickname;

    public static SimpleUser of(User user) {
        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setId(user.getId());
        simpleUser.setUsername(user.getUsername());
        simpleUser.setNickname(user.getNickname());
        return simpleUser;
    }

    public static List<SimpleUser> of(List<User> users) {
        return users.stream().map(SimpleUser::of).collect(Collectors.toList());
    }
}
