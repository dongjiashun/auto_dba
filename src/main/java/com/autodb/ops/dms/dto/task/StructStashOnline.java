package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.entity.task.StructChangeStash;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * StructChangeOnline
 *
 * @author dongjs
 * @since 2016/11/17
 */
@Data
public class StructStashOnline {
    @NotNull
    @Min(1)
    private Integer ds;

    @NotNull
    @Min(1)
    private Integer task;

    @Size(max = 50)
    private String title;

    @NotBlank
    @Size(max = Constants.SQL_MAX_SIZE)
    private String sql;

    public static StructStashOnline of(Integer ds, Integer task, List<StructChangeStash> stashes) {
        if (stashes.isEmpty()) {
            return null;
        }

        StructStashOnline online = new StructStashOnline();
        online.setDs(ds);
        online.setTask(task);

        StringBuilder sql = new StringBuilder();
        stashes.stream()
                .filter(stash -> stash.getId() <= task)
                .forEach(stash -> sql.append("# ").append(stash.getTitle()).append('\n')
                        .append(stash.getSql()).append('\n'));
        online.setSql(sql.toString());
        return online;
    }
}
