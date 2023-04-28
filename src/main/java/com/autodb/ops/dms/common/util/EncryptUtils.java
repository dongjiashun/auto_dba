package com.autodb.ops.dms.common.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * EncryptUtils
 *
 * @author dongjs
 * @since 2016-01-29
 */
public class EncryptUtils {
    private final static char[] HEXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private final static String DEFAULT_CHARSET = "UTF-8";

    public final static String ALGORITHM_DES = "DES";

    /**
     * 转换字节数组为16进制字串(HEX)
     *
     * @param bytes 字节数组
     * @return 16进制字串
     */
    public static String byte2hex(byte[] bytes) {
        StringBuilder hexStr = new StringBuilder();
        for (byte b : bytes) {
            int n = b;
            if (n < 0) {
                n = 256 + n;
            }
            int d1 = n / 16;
            int d2 = n % 16;
            hexStr.append(HEXDIGITS[d1]).append(HEXDIGITS[d2]);
        }
        return hexStr.toString();
    }

    /**
     * 转换16进制字串(HEX)为字节数组
     *
     * @param str 16进制字串
     * @return 字节数组
     */
    public static byte[] hex2byte(String str) {
        byte[] strBytes = str.getBytes();
        int strlen = strBytes.length;
        byte[] out = new byte[strlen / 2];

        for (int i = 0; i < strlen; i += 2) {
            out[i / 2] = (byte) Integer.parseInt(new String(strBytes, i, 2), 16);
        }
        return out;
    }

    /**
     * DES加密<br/>
     *
     * @param origin 明文
     * @param key    密钥
     * @throws Exception
     */
    public static byte[] DESEncrypt(byte[] origin, byte[] key) throws Exception {
        Key keySpec = DESKey(key);
        return cipherEncrypt(origin, keySpec, ALGORITHM_DES);
    }

    /**
     * DES加密<br/>
     * 使用默认编码
     *
     * @param origin 明文
     * @param key    密钥
     * @throws Exception
     */
    public static String DESEncrypt(String origin, String key) throws Exception {
        byte[] result = DESEncrypt(origin.getBytes(DEFAULT_CHARSET), key.getBytes(DEFAULT_CHARSET));
        return byte2hex(result);
    }

    /**
     * DES加密<br/>
     *
     * @param origin  明文
     * @param key     密钥
     * @param charset 编码
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    public static String DESEncrypt(String origin, String key, String charset)
            throws UnsupportedEncodingException, Exception {
        byte[] result = DESEncrypt(origin.getBytes(charset), key.getBytes(charset));
        return byte2hex(result);
    }

    /**
     * DES解密<br/>
     *
     * @param origin 明文
     * @param key    密钥
     * @throws Exception
     */
    public static byte[] DESDecrypt(byte[] origin, byte[] key) throws Exception {
        Key keySpec = DESKey(key);
        return cipherDecrypt(origin, keySpec, ALGORITHM_DES);
    }

    /**
     * DES解密<br/>
     * 使用默认编码
     *
     * @param origin 明文
     * @param key    密钥
     * @throws Exception
     */
    public static String DESDecrypt(String origin, String key) throws Exception {
        byte[] result = DESDecrypt(hex2byte(origin), key.getBytes(DEFAULT_CHARSET));
        return new String(result);
    }

    /**
     * DES解密<br/>
     *
     * @param origin  明文
     * @param key     密钥
     * @param charset 指定编码
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    public static String DESDecrypt(String origin, String key, String charset)
            throws UnsupportedEncodingException, Exception {
        byte[] result = DESDecrypt(hex2byte(origin), key.getBytes(charset));
        return new String(result);
    }

    /**
     * Java cipher 加密<br/>
     *
     * @param origin    明文
     * @param key       密钥
     * @param algorithm 算法
     * @return 密文
     * @throws Exception
     */
    public static byte[] cipherEncrypt(byte[] origin, Key key, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(origin);
    }

    /**
     * Java cipher解密<br/>
     *
     * @param origin    密文
     * @param key       密钥
     * @param algorithm 算法
     * @return 明文
     * @throws Exception
     */
    public static byte[] cipherDecrypt(byte[] origin, Key key, String algorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(origin);

    }

    /**
     * 根据传入的字符串的key生成DES key对象
     *
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private static Key DESKey(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec keySpec = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_DES);
        return keyFactory.generateSecret(keySpec);
    }
}
