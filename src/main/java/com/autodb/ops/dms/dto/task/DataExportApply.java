package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.user.User;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * DataExport Apply
 *
 * @author dongjs
 * @since 16/1/21
 */
@Data
public class DataExportApply {
    private User applyUser;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 1000)
    private String reason;

    private boolean security;

    @Size(min = 1, max = 10)
    List<Export> exports;

    /** export **/
    @Data
    public static class Export {
        private Integer ds;
        private DataSource dataSource;
        private String sql;
    }
}
