package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.entity.task.SchemaApply;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * SchemaApplyAdjust Form
 *
 * @author dongjs
 * @since 16/7/25
 * @see SchemaApply
 */
@Data
public class SchemaApplyAdjustForm {
    @NotNull
    private Boolean apply;

    @NotBlank
    @Size(max = 1000)
    private String productDesc;

    @NotBlank
    @Size(max = 1000)
    private String capacityDesc;

    private boolean split;

    @Size(max = 1000)
    private String splitDesc;

    public void setSplitDesc(String splitDesc) {
        this.splitDesc = splitDesc;
        this.split = StringUtils.isNotBlank(splitDesc);
    }
}
