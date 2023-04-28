package com.autodb.ops.dms.domain.bi;

//import com.autodb.cryption.CryptionClient;
import com.google.common.collect.Lists;
import com.autodb.ops.dms.common.Constants;
import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.domain.datasource.visitor.Result;
import com.autodb.ops.dms.domain.datasource.visitor.ResultHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Encryption Service
 *
 * @author dongjs
 * @since 16/5/11
 */
@Component
public class EncryptionService {
    private static Logger log = LoggerFactory.getLogger(EncryptionService.class);

    private static final String HIDDEN_PREFIX = Constants.HIDDEN_PREFIX;
    private static final String ENCRYPT_TYPE_NAME = JDBCType.VARCHAR.getName() + "-SEC";

    @Value("${api.encryption.service}")
    private String endPoint;

    @Value("${api.encryption.token}")
    private String token;

    @Value("${api.encryption.secret}")
    private String secret;

    public String decrypt(String data, String user) {
        /*CryptionClient client = null;
        try {
            long start = System.currentTimeMillis();
            client = new CryptionClient(user, endPoint, token, secret);
            String secretData = client.decrypt(data);
            long end = System.currentTimeMillis();
            log.info("CryptionClient decrypt data, user {}, cost {} ms", user, end - start);
            return secretData;
        } catch (Exception e) {
            log.warn("CryptionClient decrypt data exception: {}", e.getMessage());
            return "CryptionClient decrypt data exception";
        } finally {
            if (client != null) {
                try {
                    client.shutdown();
                } catch (Exception e) {
                    log.warn("shutdown CryptionClient exception: {}", e.getMessage());
                    // ignore
                }
            }
        }*/
        try {
            data = DataSourceEncryptUtils.decrypt(data,secret);
        } catch (Exception e) {
            log.warn("解密数据={} 异常exception={}",data,e.getMessage());
        }
        return data;
    }

    public void encryptResult(Result result, String user) {
        List<Map<String, Object>> data = result.getData();
        ResultHeader header = result.getHeader();
        if (data.size() > 0 && header.getColumnCount() > 0) {
            // index -> column name
            List<String> fields = new ArrayList<>();
            List<Integer> fieldIndexs = new ArrayList<>();
            for (int i = 0; i < header.getColumnNames().length; i++) {
                String column = header.getColumnNames()[i];
                if (column.startsWith(HIDDEN_PREFIX)) {
                    fields.add(column.substring(HIDDEN_PREFIX.length()));
                    fieldIndexs.add(i);
                }
            }

            int fieldSize = fields.size();
            if (fieldSize > 0) {
                // get and delete origin data
                List<String> originData = new ArrayList<>();
                data.forEach(column -> fields.forEach(field ->
                        originData.add(String.valueOf(column.remove(HIDDEN_PREFIX + field)))));

                // encrypt data
                List<String> secretData = encrypt(originData, user);
                if (secretData.size() > 0) {
                    // change type
                    fieldIndexs.forEach(index -> header.getColumnTypeNames()[index - 1] = ENCRYPT_TYPE_NAME);
                    // set secret data
                    for (int col = 0; col < data.size(); col++) {
                        Map<String, Object> column = data.get(col);
                        for (int i = 0; i < fieldSize; i++) {
                            column.replace(fields.get(i), secretData.get(col * fieldSize + i));
                        }
                    }
                }

                // delete header column
                int[] indices = new int[fieldIndexs.size()];
                for (int i = 0; i < fieldIndexs.size(); i++) {
                    indices[i] = fieldIndexs.get(i);
                }
                header.removeColumn(indices);
            }
        }
    }

    /**
     * 加密失败返回empty list
     **/
    private List<String> encrypt(List<String> data, String user) {
        /*CryptionClient client = null;
        try {
            long start = System.currentTimeMillis();
            client = new CryptionClient(user, endPoint, token, secret);
            List<String> secretData = client.encryptBatch(data);
            long end = System.currentTimeMillis();
            log.info("CryptionClient encryptBatch user {} count {}, cost {} ms", user, data.size(), end - start);
            return secretData.size() == data.size() ? secretData : Collections.emptyList();
        } catch (Exception e) {
            log.warn("CryptionClient encryptBatch exception: {}", e.getMessage());
            return Collections.emptyList();
        } finally {
            if (client != null) {
                try {
                    client.shutdown();
                } catch (Exception e) {
                    log.warn("shutdown CryptionClient exception: {}", e.getMessage());
                    // ignore
                }
            }
        }*/
        List<String> encryData = Lists.newArrayList();
        for(String s : data){
            try {
                s = DataSourceEncryptUtils.encrypt(s,secret);
            } catch (Exception e) {
                log.warn("加密数据={} 异常exception={}",s,e.getMessage());
            }
            encryData.add(s);
        }
        return encryData;
    }
}
