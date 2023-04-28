package com.autodb.ops.dms.domain.email;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Email Do
 *
 * @author dongjs
 * @since 16/1/20
 */
@Data
public class EmailDo {
    private String template;
    private List<String> to;
    private List<String> cc;

    private String subject;
    private Map<String, Object> context;
}
