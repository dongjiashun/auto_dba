package com.autodb.ops.dms.dto.caas;

import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.entity.task.StructChangeStash;
import com.autodb.ops.dms.entity.user.User;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * StructChange Stash
 *
 * @author dongjs
 * @since 16/12/29
 */
@Data
public class ChangeStash {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotNull
    private Integer ds;

    @NotNull
    private Byte type;

    @NotBlank
    @Size(max = Constants.SQL_MAX_SIZE)
    private String sql;

    public StructChangeStash toStructChangeStash(User user) {
        return StructChangeStash.builder()
                .user(user)
                .title(title)
                .ds(ds)
                .changeType(type)
                .sql(sql)
                .gmtCreate(new Date())
                .build();
    }
}
