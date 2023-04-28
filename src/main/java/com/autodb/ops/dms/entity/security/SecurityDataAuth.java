package com.autodb.ops.dms.entity.security;

import com.autodb.ops.dms.entity.user.User;
import lombok.Data;

import java.util.Date;

/**
 * SecurityData Auth
 *
 * @author dongjs
 * @since 16/1/28
 */
@Data
public class SecurityDataAuth {
    private int id;
    private SecurityData security;
    private User user;
    private Date gmtCreate;
}
