package com.autodb.ops.dms.entity.sys;

import lombok.Data;

import java.util.Date;

/**
 * Broadcast
 *
 * @author dongjs
 * @since 2016/11/9
 */
@Data
public class Broadcast {
    private int id;
    private Date start;
    private Date end;
    private String message;
    private Date createTime;
}
