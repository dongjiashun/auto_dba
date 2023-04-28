package com.autodb.ops.dms.repository.datasource.impl;

import com.autodb.ops.dms.Main;
import com.autodb.ops.dms.entity.datasource.DataSourceAuth;
import com.autodb.ops.dms.repository.datasource.DataSourceAuthDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * DataSourceAuthDaoImpl Test
 * @author dongjs
 * @since 16/5/18
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest(randomPort = true)
public class DataSourceAuthDaoImplTest {
    @Autowired
    private DataSourceAuthDao dataSourceAuthDao;

    @Test
    public void findByDataSourceRoles() throws Exception {
        List<DataSourceAuth> authList;
        authList = dataSourceAuthDao.findByDataSourceRoles(1, Collections.singletonList("owner"));
        authList.forEach(System.out::println);

        authList = dataSourceAuthDao.findByDataSourceRoles(1, Arrays.asList("owner", "reviewer"));
        authList.forEach(System.out::println);
    }

}