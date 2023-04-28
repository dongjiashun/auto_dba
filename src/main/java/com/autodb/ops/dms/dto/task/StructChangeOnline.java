package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.entity.task.StructChange;
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
public class StructChangeOnline {
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

    public static StructChangeOnline of(Integer ds, Integer task, List<StructChange> changes) {
        if (changes.isEmpty()) {
            return null;
        }

        StructChangeOnline online = new StructChangeOnline();
        online.setDs(ds);
        online.setTask(task);

        StringBuilder sql = new StringBuilder();
        changes.forEach(change -> {
            sql.append("# ").append(change.getTask().getExplain()).append('\n').append(change.getSql()).append('\n');
        });
        online.setSql(sql.toString());
        return online;
    }
}
