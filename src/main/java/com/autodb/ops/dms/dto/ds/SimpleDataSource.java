package com.autodb.ops.dms.dto.ds;

import com.autodb.ops.dms.entity.datasource.DataSource;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User un auth DataSource
 *
 * @author dongjs
 * @since 2015/1/14
 */
@Data
public class SimpleDataSource {
    private Integer id;
    private String name;
    private String sid;
    private String type;

    public static SimpleDataSource of(DataSource dataSource) {
        SimpleDataSource source = new SimpleDataSource();
        source.setId(dataSource.getId());
        source.setName(dataSource.getName());
        source.setType(dataSource.getType());
        source.setSid(dataSource.getSid());
        return source;
    }

    public static List<SimpleDataSource> of(List<DataSource> dataSources) {
        return dataSources.stream().map(SimpleDataSource::of).collect(Collectors.toList());
    }
}
