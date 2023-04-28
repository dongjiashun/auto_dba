package com.autodb.ops.dms.entity.datasource;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class Department {
    private Integer id;

    @NotNull
    @Size(min = 1)
    private String dbname;

    @NotNull
    @Size(min = 1)
    private String dbinstance;

    @Size(min = 1)
    private String username;

    @Size(min = 1)
    private String passwd;

    @NotNull
    @Size(min = 1)
    private String team;

    @NotNull
    @Max(255)
    @Min(0)
    private Integer slowlog;

    @NotNull
    @Max(255)
    @Min(0)
    private Integer sqlkill;
}
