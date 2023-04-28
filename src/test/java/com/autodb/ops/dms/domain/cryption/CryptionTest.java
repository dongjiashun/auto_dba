package com.autodb.ops.dms.domain.cryption;

//import com.autodb.cryption.CryptionClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cryption Test
 *
 * @author dongjs
 * @since 16/4/26
 */
/*public class CryptionTest {
    private CryptionClient client;

    // ldap 账户名 ，即花名拼音
    private String userName = "dongjs";

    @Before
    public void setUp() throws Exception {
        // 请求服务域名，一般固定
        String endPoint = "192.168.3.113:10010";
        // 系统生成的token及密钥
        String token = "9e674855e9c862068340806d3094313a";
        String secret = "414210df";
        client = new CryptionClient(userName, endPoint, token, secret);
    }

    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.shutdown();
        }
    }

    @Test
    public void testSecrecyMetas() throws Exception {
        client.getSecrecyMetas().forEach(System.out::println);
    }

    @Test(timeout = 1000)
    public void testEncrypt() throws Exception {
        String message = "18758121786";
        // 加密一条
        String encryptMessage = client.encrypt(message);
        // 解密单条
        String decryptMessage = client.decrypt(encryptMessage);
        Assert.assertEquals(message, decryptMessage);

        System.out.println("密文为：" + encryptMessage);
        System.out.println("明文为：" + decryptMessage);
    }

    @Test
    public void testEncryptBatch1K() throws Exception {
        testEncryptBatch(1000);
    }

    @Test
    public void testEncryptBatch10K() throws Exception {
        testEncryptBatch(10000);
    }

    @Test
    public void testEncryptBatch50K() throws Exception {
        testEncryptBatch(50000);
    }

    @Test
    public void testEncryptBatch100K() throws Exception {
        testEncryptBatch(100000);
    }

    private void testEncryptBatch(int size) throws Exception {
        List<String> messageList = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            messageList.add(UUID.randomUUID().toString());
            // messageList.add("1");
        }

        long start = System.currentTimeMillis();
        // 批量加密
        List<String> encryptMessageList = client.encryptBatch(messageList);
        long encrypt = System.currentTimeMillis();
        // 批量解密
        List<String> decryptMessageList = client.decryptBatch(encryptMessageList);
        long decrypt = System.currentTimeMillis();

        System.out.println(size + " encrypt cost: " + (encrypt - start) + "ms");
        System.out.println(size + " decrypt cost: " + (decrypt - encrypt) + "ms");

        // System.out.println("批量加密列表：" + encryptMessageList);
        // System.out.println("批量解密列表：" + decryptMessageList);
    }
}*/
