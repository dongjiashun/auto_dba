package com.autodb.ops.dms.entity.task;

import com.autodb.ops.dms.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Struct Change Stash
 *
 * @author dongjs
 * @since 16/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructChangeStash {
    private Integer id;
    private User user;
    private String title;
    private Integer ds;
    private byte changeType;
    private String sql;
    private Date gmtCreate;
}
