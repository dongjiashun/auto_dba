package com.autodb.ops.dms.entity.datasource;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * DataSource Proxy
 * @author dongjs
 * @since 16/4/19
 */
@Data
public class DataSourceProxy {
    private Integer id;
    @NotNull
    @Size(max = 30)
    private String name;
    @NotNull
    @Size(max = 30)
    private String host;

    private Date gmtCreate;
    @Null
    private Date gmtModified;

    List<DataSource> dataSources;
}
