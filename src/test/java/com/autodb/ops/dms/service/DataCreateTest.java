package com.autodb.ops.dms.service;

import com.autodb.ops.dms.Main;
import com.autodb.ops.dms.domain.datasource.DataSourceManager;
import com.autodb.ops.dms.domain.datasource.visitor.ConnectionInfo;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest(randomPort = true)
public class DataCreateTest {

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Test
    public void testInsertData(){
        DataSource dataSource = dataSourceDao.findByEnvSid(DataSource.Env.DEV,"dms");
            DatabaseVisitor databaseVisitor = dataSourceManager.getDatabaseVisitor(dataSource.mainConnectionInfo());
            for(int i = 0;i<100000;i++){
                String name = "xlpxlpadf"+i;
                String name1 = name + RandomUtils.nextInt(0,1000000);
                String sql = "insert into test_change (`name`) values('"+name+"')"+","+"('"+name1+"') ;";
                try {
                    databaseVisitor.update(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
    }
}
