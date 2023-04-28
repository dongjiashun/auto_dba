package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.user.User;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * DataChange Apply
 *
 * @author dongjs
 * @since 16/1/25
 */
@Data
public class DataChangeApply {
    private User applyUser;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 1000)
    private String reason;

    @Size(min = 1, max = 10)
    List<Change> changes;

    /** change **/
    @Data
    public static class Change {
        private Integer ds;
        private DataSource dataSource;
        private String sql;
    }
}
