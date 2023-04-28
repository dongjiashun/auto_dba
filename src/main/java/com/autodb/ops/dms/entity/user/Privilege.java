package com.autodb.ops.dms.entity.user;

import lombok.Data;

/**
 * privilege
 * @author dongjs
 * @since 2015/11/5
 */
@Data
public class Privilege {
    private int id;
    private String name;
    private String code;
    private String desc;
}
