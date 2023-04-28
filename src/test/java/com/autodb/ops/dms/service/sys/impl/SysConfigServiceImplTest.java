package com.autodb.ops.dms.service.sys.impl;

import com.autodb.ops.dms.Main;
import com.autodb.ops.dms.service.sys.SysConfigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author dongjs
 * @since 16/7/23
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest(randomPort = true)
public class SysConfigServiceImplTest {
    @Autowired
    private SysConfigService sysConfigService;

    @Test
    public void findValue() throws Exception {
        System.out.println(sysConfigService.findValue("sys.menus"));
    }

    @Test
    public void findListValue() throws Exception {
        System.out.println(sysConfigService.findListValue("sys.products"));
        System.out.println(sysConfigService.findListValue("sys.scenes"));
    }

}