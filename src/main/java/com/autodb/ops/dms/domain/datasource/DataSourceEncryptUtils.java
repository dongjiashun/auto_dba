package com.autodb.ops.dms.domain.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.datasource.DataSourceCobar;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * DataSourceEncrypt Utils
 *
 * @author dongjs
 * @since 16/1/29
 */
public final class DataSourceEncryptUtils {
    private static Logger logger = LoggerFactory.getLogger(DataSourceEncryptUtils.class);

    public static void encryptPassword(DataSource dataSource) throws AppException {
        try {
            String newPwd = encrypt(dataSource.getPassword(),
                    dataSource.getHost() + dataSource.getUsername());
            String newPwd2 = encrypt(dataSource.getPassword2(),
                    dataSource.getHost2() + dataSource.getUsername2());
            dataSource.setPassword(newPwd);
            dataSource.setPassword2(newPwd2);

            if (dataSource.getProxyPassword() != null) {
                String newProxyPwd = encrypt(dataSource.getProxyPassword(),
                        dataSource.getHost() + dataSource.getUsername());
                dataSource.setProxyPassword(newProxyPwd);
            }
        } catch (Exception e) {
            throw new AppException(ExCode.SYS_003, e);
        }
    }

    public static void decryptPassword(DataSource dataSource) {
        dataSource.setPassword(getDecryptPassword1(dataSource));
        dataSource.setPassword2(getDecryptPassword2(dataSource));
        dataSource.setProxyPassword(getDecryptProxyPassword(dataSource));
    }

    public static String getDecryptPassword1(DataSource dataSource) {
        try {
            return decrypt(dataSource.getPassword(),
                    dataSource.getHost() + dataSource.getUsername());
        } catch (Exception e) {
            logger.error("decrypt password error", e);
            return "";
        }
    }

    public static String getDecryptPassword2(DataSource dataSource) {
        try {
            return decrypt(dataSource.getPassword2(),
                    dataSource.getHost2() + dataSource.getUsername2());
        } catch (Exception e) {
            logger.error("decrypt password2 error", e);
            return "";
        }
    }

    public static String getDecryptProxyPassword(DataSource dataSource) {
        try {
            return dataSource.getProxyPassword() == null ? null : decrypt(dataSource.getProxyPassword(),
                    dataSource.getHost() + dataSource.getUsername());
        } catch (Exception e) {
            logger.error("decrypt proxy password error", e);
            return "";
        }
    }

    /**
     * 加密
     *
     * @param plaintext 原文
     * @param key       密钥
     */
    public static String encrypt(String plaintext, String key) throws UnsupportedEncodingException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        String cipherText = null;

        if (plaintext != null && key != null) {
            byte[] byteContent = plaintext.getBytes("utf-8");
            byte[] digest = digest(byteContent, key, true);
            if (digest != null) {
                // 转成十六进制字符串
                cipherText = parseByte2HexStr(digest);
            }
        }
        return cipherText;
    }

    /**
     * 解密
     *
     * @param ciphertext 密文
     * @param key        密钥
     */
    public static String decrypt(String ciphertext, String key) throws NoSuchPaddingException,
            BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
            UnsupportedEncodingException, InvalidKeyException {
        String plaintext = null;

        if (ciphertext != null && key != null) {
            byte[] byteContent = parseHexStr2Byte(ciphertext);
            byte[] digest = digest(byteContent, key, false);

            if (digest != null) {
                plaintext = new String(digest);
            }
        }
        return plaintext;
    }


    /**
     * 将字节数组转成十六进制字符串
     */
    private static String parseByte2HexStr(byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                String hex = Integer.toHexString(aByte);
                if (hex.length() > 2) {
                    hex = hex.substring(hex.length() - 2);
                }
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }

            return sb.toString();
        }

        return null;
    }

    /**
     * 将十六进制字符串转换为字节数组
     */
    private static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr != null) {
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; i++) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }
            return result;
        }

        return null;
    }

    private static byte[] digest(byte[] byteContent, String key, boolean encrypt) throws NoSuchPaddingException,
            NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        byte[] digest;
        // 生成密钥
        byte[] kb = key.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        kb = sha.digest(kb);
        kb = Arrays.copyOf(kb, 16);
        // use only first 128 bit
        SecretKeySpec secretKeySpec = new SecretKeySpec(kb, "AES");

        // 创建密码器
        Cipher cipher = Cipher.getInstance("AES");

        int mode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

        // 初始化
        cipher.init(mode, secretKeySpec);
        digest = cipher.doFinal(byteContent);
        return digest;
    }

    private DataSourceEncryptUtils() {
    }

    public  static String decryptSharding(String ciphertext, String key){
        try {
            String result = decrypt(ciphertext,key);
            return result;
        } catch (Exception e) {
            logger.error("decrypt password error", e);
            return "";
        }
    }
    public static void main(String[] args) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        String password = "274836Bdec";
        String host = "172.24.48.4";
        String userName = "prdquery";
        String ciphertext = password;
        String key = host+userName;

        String encresult = encrypt(ciphertext,key);
        System.out.println(encresult);
        String decresult = decrypt(encresult,key);
        System.out.println(decresult);
    }
}
