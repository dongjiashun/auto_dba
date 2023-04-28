package com.autodb.ops.dms.domain.activiti;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceAuthDao;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.user.UserDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CandidateUsersService Impl
 *
 * @author dongjs
 * @since 16/1/13
 */
@Component
public class CandidateUsersService {
    private static final String ROLE_OWNER = "owner";
    private static final String ROLE_REVIEWER = "reviewer";
    private static final String ROLE_EXPORTER = "exporter";

    public static final String ROLE_DBA = "dba";

    @Value("${task.candidate.alternate}")
    private String alternate;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DataSourceAuthDao dataSourceAuthDao;

    public List<String> dataSourceOwner(String dataSource) throws AppException {
        return this.dataSourceOwner(dataSource, null);
    }

    public List<String> dataSourceOwner(String dataSource, String applyUser) throws AppException {
        return this.users(dataSource, applyUser, ROLE_OWNER);
    }

    public List<String> dataSourceReviewerOwner(String dataSource) throws AppException {
        return this.dataSourceReviewerOwner(dataSource, null);
    }

    public List<String> dataSourceReviewerOwner(String dataSource, String applyUser) throws AppException {
        return this.users(dataSource, applyUser, ROLE_REVIEWER, ROLE_OWNER);
    }

    public List<String> dataSourceExporterOwner(String dataSource) throws AppException {
        return this.dataSourceExporterOwner(dataSource, null);
    }

    public List<String> dataSourceExporterOwner(String dataSource, String applyUser) throws AppException {
        return this.users(dataSource, applyUser, false, ROLE_EXPORTER, ROLE_REVIEWER, ROLE_OWNER);
    }

    public List<String> systemDBAExclude(String applyUser) throws AppException {
        List<User> userList = userDao.findByRole(ROLE_DBA);
        Set<String> userNameList = Sets.newHashSet();
        for(User user : userList){
            if(!user.getUsername().equals(applyUser))
                userNameList.add(user.getUsername());
        }
        if(applyUser.equals("04915")){
            userNameList.add("04915");
        }
        return ofUserList(userNameList);
    }

    public List<String> systemDBAMobileExclude(String applyUser) throws AppException {
        List<User> userList = userDao.findByRole(ROLE_DBA);
        List<String> mobileList = Lists.newArrayList();
        for(User user : userList){
            if(!user.getUsername().equals(applyUser))
                mobileList.add(user.getMobile());
        }
        return mobileList;
    }

    public List<String> systemDBA() throws AppException {
        return ofUserList(userDao.findByRole(ROLE_DBA).stream().map(User::getUsername).collect(Collectors.toSet()));
    }

    /**
     * users
     * @param applyUser remove when equals
     */
    private List<String> users(String dataSource, String applyUser, boolean excludeApplyUser, String... roles) {
        Set<String> users = Collections.emptySet();
        DataSource ds = dataSourceDao.find(dataSource);
        if (null != ds) {
            if (!DataSource.Env.PROD.equals(ds.getEnv())) {
                users = Collections.singleton(applyUser != null ? applyUser : alternate);
            } else {
                users = dataSourceAuthDao.findByDataSourceRoles(ds.getId(), Arrays.asList(roles)).stream()
                        .map(auth -> auth.getUser().getUsername())
                        .filter(user -> !excludeApplyUser || !user.equals(applyUser))
                        .collect(Collectors.toSet());
            }
        }

        return ofUserList(users);
    }

    private List<String> users(String dataSource, String applyUser, String... roles) {
        return users(dataSource, applyUser, true, roles);
    }

    /** of User List **/
    private List<String> ofUserList(Set<String> users) {
        // hack when no owner
        return users != null && users.size() > 0 ? new ArrayList<>(users) : Collections.singletonList(alternate);
    }
}
