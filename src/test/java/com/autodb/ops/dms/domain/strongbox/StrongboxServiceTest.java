package com.autodb.ops.dms.domain.strongbox;

import com.autodb.ops.dms.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

/**
 * StrongBoxService Test
 * @author dongjs
 * @since 16/4/21
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest(randomPort = true)
public class StrongboxServiceTest {
    @Autowired
    private StrongboxService strongboxService;

    @Test
    public void testKey() throws Exception {
        System.out.println(strongboxService.key("603"));
    }

    @Test
    public void testImportDataSources() throws Exception {
        StrongboxService.JdbcPropertiesSet jdbcPropertiesSet = new StrongboxService.JdbcPropertiesSet(Collections
                .singleton(new StrongboxService.JdbcProperties("wac", "10.0.0.1:3306", "sa", "sa")));
        strongboxService.importDataSources(jdbcPropertiesSet);

        strongboxService.removeDataSource("wac");
    }
}