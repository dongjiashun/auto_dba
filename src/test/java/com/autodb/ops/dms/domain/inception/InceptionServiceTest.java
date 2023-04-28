package com.autodb.ops.dms.domain.inception;

import com.autodb.ops.dms.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * InceptionService Test
 *
 * @author dongjs
 * @since 16/5/26
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest(randomPort = true)
public class InceptionServiceTest {
    @Autowired
    private InceptionService inceptionService;

    private String ds = "192.168.131.129:3306:dms:root:123456";
    private String sql = "CREATE TABLE `assets_privacy_setting_001` (\n" +
            "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT comment '主键',\n" +
            "  `uid` bigint(20) unsigned NOT NULL comment '用户id',\n" +
            "  `chart_visibility` tinyint(3) unsigned DEFAULT '0' COMMENT '资产配置图隐私　1-不隐藏　0-隐藏',\n" +
            "  `rate_visibility` tinyint(3) unsigned DEFAULT '0' COMMENT '资产评分隐私　   1-不隐藏　0－隐藏',\n" +
            "  `income_visibility` tinyint(3) unsigned DEFAULT '0' COMMENT '年收益率隐私      1-不隐藏　0－隐藏',\n" +
            "  `capital_visibility` tinyint(3) unsigned DEFAULT '0' COMMENT '持仓信息隐私　   1－不隐藏 0－隐藏',\n" +
            "  `assets_visibility` tinyint(3) unsigned DEFAULT '0' COMMENT '社区资产配置图　1－不隐藏　0－隐藏',\n" +
            "  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  comment '创建时间',\n" +
            "  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  KEY `idx_uid` (`uid`)\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=810 DEFAULT CHARSET=utf8 COMMENT='个人资产配置隐私设置';";

    @Test
    public void check() throws Exception {
        List<InceptionService.Result> check = inceptionService.check(sql, ds,false);
        List<InceptionService.Result> check1 = inceptionService.check(sql, ds,false);
        check.forEach(System.out::println);
    }

    @Test
    public void execute() throws Exception {
        List<InceptionService.Result> execute = inceptionService.execute(sql, ds);
        execute.forEach(System.out::println);
    }

    @Test
    public void asyncExecute() throws Exception {
        String hash = inceptionService.asyncExecute("alter table test add `o_all_localz1111` tinyint(4) DEFAULT NULL comment 'aa';", "115.239.208.211:3306:dms:devuser:Devuser123");
        System.out.println(hash);

        // TimeUnit.SECONDS.sleep(1);

        InceptionService.ProgressResult progress = inceptionService.execProgress(hash);
        System.out.println(progress);
    }

    @Test
    public void execProgress() throws Exception {
        InceptionService.ProgressResult progress = inceptionService.execProgress("5fa9b2caf7b9d7067a28f6141a445226");
        System.out.println(progress);
    }
}