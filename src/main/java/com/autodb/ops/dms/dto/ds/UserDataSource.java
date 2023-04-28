package com.autodb.ops.dms.dto.ds;

import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.entity.datasource.DataSourceRole;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User DataSource
 *
 * @author dongjs
 * @since 2015/12/31
 */
@Data
public class UserDataSource {
    private Integer id;
    private String env;
    private String name;
    private String sid;
    private String type;
    private Date auth;
    private Integer authId;
    private List<DataSourceRole> roles;

    public static UserDataSource of(DataSourceAuth auth) {
        UserDataSource dataSource = new UserDataSource();
        dataSource.setEnv(auth.getDataSource().getEnv());
        dataSource.setId(auth.getDataSource().getId());
        dataSource.setName(auth.getDataSource().getName());
        dataSource.setSid(auth.getDataSource().getSid());
        dataSource.setType(auth.getDataSource().getType());
        dataSource.setAuth(auth.getGmtAuth());
        dataSource.setAuthId(auth.getId());
        dataSource.setRoles(auth.getRoles());

        return dataSource;
    }

    public static List<UserDataSource> of(List<DataSourceAuth> authList) {
        return authList.stream().map(UserDataSource::of).collect(Collectors.toList());
    }
}
