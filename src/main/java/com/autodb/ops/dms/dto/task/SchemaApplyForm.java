package com.autodb.ops.dms.dto.task;

import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.task.SchemaApply;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

/**
 * SchemaApply Form
 *
 * @author dongjs
 * @since 16/7/25
 * @see com.autodb.ops.dms.entity.task.SchemaApply
 */
@Data
public class SchemaApplyForm {
    @NotBlank
    @Size(max = 10)
    private String env;

    @NotBlank
    @Size(max = 30)
    private String sid;

    @NotBlank
    @Size(max = 50)
    private String product;

    @NotBlank
    @Size(max = 50)
    private String scene;

    @NotBlank
    @Size(max = 1000)
    private String productDesc;

    @NotBlank
    @Size(max = 1000)
    private String capacityDesc;

    @Size(max = 1000)
    private String splitDesc;

    public SchemaApply toSchemaApply() {
        SchemaApply schemaApply = new SchemaApply();
        schemaApply.setEnv(DataSource.Env.getEnv(env));
        schemaApply.setSid(sid);
        schemaApply.setProduct(product);
        schemaApply.setScene(scene);
        schemaApply.setProductDesc(productDesc);
        schemaApply.setCapacityDesc(capacityDesc);
        schemaApply.setSplit(StringUtils.isNotEmpty(splitDesc));
        schemaApply.setSplitDesc(splitDesc);
        return schemaApply;
    }
}
