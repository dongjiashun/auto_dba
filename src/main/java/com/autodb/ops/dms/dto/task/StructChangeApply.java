package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.user.User;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * StructChange Apply
 *
 * @author dongjs
 * @since 16/5/27
 */
@Data
public class StructChangeApply {
    private User applyUser;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 1000)
    private String reason;

    @Size(min = 1, max = 10)
    private List<Change> changes;

    /** change **/
    @Data
    public static class Change {
        private Integer ds;
        private DataSource dataSource;
        private Byte type;
        private String sql;

        private boolean online;
        private Integer lastChangeId;
        private Date lastChangeTime;


        public Change() {
        }

        public Change(Integer ds, Byte type, String sql) {
            this.ds = ds;
            this.type = type;
            this.sql = sql;
        }
    }
}
