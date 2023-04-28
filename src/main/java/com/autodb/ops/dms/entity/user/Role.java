package com.autodb.ops.dms.entity.user;

import lombok.Data;

import java.util.List;

/**
 * role
 * @author dongjs
 * @since 2015/11/5
 */
@Data
public class Role {
    private int id;
    private String code;
    private String name;
    private List<Privilege> privileges;
}
