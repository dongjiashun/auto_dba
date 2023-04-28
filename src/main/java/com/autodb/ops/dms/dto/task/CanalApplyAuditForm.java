package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.entity.task.CanalApply;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * CanalApplyAudit Form
 *
 * @author dongjs
 * @since 2016/11/3
 */
@Data
public class CanalApplyAuditForm {
    @NotNull
    private Byte agree;

    @Min(0)
    private int manager;

    @Size(max = 100)
    private String target;

    @Min(0)
    private int index;

    @Size(max = 100)
    private String key;

    @NotBlank
    @Size(max = 255)
    private String reason;

    public void setAgree(Byte agree) {
        this.agree = CanalApply.toAssessType(agree);
    }

    public void applyTo(CanalApply canalApply) {
        canalApply.setManager(manager);
        canalApply.setTarget(target);
        canalApply.setIndex(index);
        canalApply.setKey(key);
    }
}
