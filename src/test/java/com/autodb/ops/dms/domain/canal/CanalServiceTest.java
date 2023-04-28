package com.autodb.ops.dms.domain.canal;

import com.autodb.ops.dms.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * CanalService Test
 * @author dongjs
 * @since 2016/10/26
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest(randomPort = true)
public class CanalServiceTest {
    @Autowired
    private CanalService canalService;

    @Test
    public void testImportDatasource() throws Exception {
        CanalService.Result<String> result = canalService.importDatasource("001.pub.mysql.qa.autodb.info", 3307);
        System.out.println(result);
    }

    @Test
    public void testDeleteDatasource() throws Exception {
        CanalService.Result<String> result = canalService.deleteDatasource("192.168.5.12", 3308);
        System.out.println(result);
    }

    @Test
    public void testManage() throws Exception {
        CanalService.Result<?> result = canalService.manager("192.168.5.12", 3308);
        System.out.println(result);
    }

    @Test
    public void testAddSync() throws Exception {
        CanalService.Result<String> result = canalService.addSync("192.168.5.12", 3308,
                "information_schema", "CHARACTER_SETS", "db.information_schema.CHARACTER_SETS", 1,  "dfdfsd", 2);
        System.out.println(result);
    }
}