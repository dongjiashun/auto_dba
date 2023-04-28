package com.autodb.ops.dms.entity.user;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * user
 * @author dongjs
 * @since 2015/11/5
 */
@Data
public class User {
    private Integer id;
    /** 花名拼音 **/
    private String username;
    private String nickname;
    private String email;
    private String mobile;
    private Date gmtCreate;
    private Date gmtModified;

    private List<Role> roles;

    public static User of(int id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    public User() {
        super();
    }

    public User(User user) {
        this.id = user.id;
        this.username = user.username;
        this.nickname = user.nickname;
        this.email = user.email;
        this.mobile = user.mobile;
        this.gmtCreate = user.gmtCreate;
        this.gmtModified = user.gmtModified;
        this.roles = user.roles;
    }
}
