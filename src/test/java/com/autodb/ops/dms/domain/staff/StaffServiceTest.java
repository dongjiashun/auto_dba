package com.autodb.ops.dms.domain.staff;

import com.autodb.ops.dms.Main;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * StaffService Test
 * @author dongjs
 * @since 16/3/18
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebIntegrationTest(randomPort = true)
public class StaffServiceTest {
    @Autowired
    private StaffService staffService;

    @Test
    public void testStaffInfo() throws Exception {
//        String name = "dongjs";
        String userCode = "04530";
        StaffService.User dongjs = staffService.staffInfo(userCode);
        Assert.assertEquals(dongjs.getId()+"", "18623");
    }
}