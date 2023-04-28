package com.autodb.ops.dms.service.security.impl;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.security.SecurityAuthQuery;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.security.SecurityData;
import com.autodb.ops.dms.entity.security.SecurityDataAuth;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.security.SecurityDataAuthDao;
import com.autodb.ops.dms.repository.security.SecurityDataDao;
import com.autodb.ops.dms.repository.user.UserDao;
import com.autodb.ops.dms.service.security.SecurityDataAuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SecurityDataAuthService Impl
 * @author dongjs
 * @since 16/1/28
 */
@Service
public class SecurityDataAuthServiceImpl implements SecurityDataAuthService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private SecurityDataDao securityDataDao;

    @Autowired
    private SecurityDataAuthDao securityDataAuthDao;

    @Override
    @Transactional
    public void add(List<Integer> securityIds, List<String> users) throws AppException {
        List<SecurityData> securityDataList = securityDataDao.findByIds(securityIds);
        List<User> userList = userDao.findByUsernames(users);

        for (SecurityData security : securityDataList) {
            for (User user : userList) {
                SecurityDataAuth auth = securityDataAuthDao.findBySecUser(security.getId(), user.getUsername());
                if (auth == null) {
                    auth = new SecurityDataAuth();
                    auth.setSecurity(security);
                    auth.setUser(user);
                    auth.setGmtCreate(new Date());
                    securityDataAuthDao.add(auth);
                }
            }
        }
    }

    @Override
    @Transactional
    public int delete(List<Integer> ids) throws AppException {
        return securityDataAuthDao.delete(ids);
    }

    @Override
    public List<SecurityDataAuth> findByQuery(SecurityAuthQuery query, Page<SecurityDataAuth> page)
            throws AppException {
        List<SecurityDataAuth> securityDataAuthList;
        if (query.getDatasource() == null && StringUtils.isBlank(query.getUsername())) {
            securityDataAuthList = securityDataAuthDao.findAll(page);
        } else if (query.getDatasource() == null) {
            securityDataAuthList = securityDataAuthDao.findByUser(query.getUsername(), page);
        } else {
            securityDataAuthList = securityDataAuthDao.findByDsUser(query.getDatasource(), query.getUsername(), page);
        }
        injectSecurityData(securityDataAuthList);

        page.setData(securityDataAuthList);
        return securityDataAuthList;
    }

    private void injectSecurityData(List<SecurityDataAuth> securityDataAuthList) {
        List<Integer> secIds = securityDataAuthList.stream().map(auth -> auth.getSecurity().getId()).collect(Collectors.toList());

        List<SecurityData> securityDataList = securityDataDao.findByIds(secIds);
        List<Integer> dsIds = securityDataList.stream()
                .map(data -> data.getDataSource().getId())
                .collect(Collectors.toList());
        Map<Integer, DataSource> dataSourceMap = dataSourceDao.findMap(dsIds);

        // inject datasource
        securityDataList.forEach(data -> {
            DataSource dataSource = dataSourceMap.get(data.getDataSource().getId());
            if (dataSource != null) {
                data.setDataSource(dataSource);
            }
        });

        // inject security data
        Map<Integer, SecurityData> securityDataMap = securityDataList.stream()
                .collect(Collectors.toMap(SecurityData::getId, data -> data));
        securityDataAuthList.forEach(data -> {
            SecurityData securityData = securityDataMap.get(data.getSecurity().getId());
            if (securityData != null) {
                data.setSecurity(securityData);
            }
        });
    }
}
