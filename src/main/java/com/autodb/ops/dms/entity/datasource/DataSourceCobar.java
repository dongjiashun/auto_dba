package com.autodb.ops.dms.entity.datasource;

import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.domain.datasource.visitor.ConnectionInfo;
import lombok.Data;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataSourceCobar
 * @author dongjs
 * @since 2016/10/24
 */
@Data
public class DataSourceCobar {
    private static ObjectMapper objectMapper = new ObjectMapper();

    private Integer id;
    private DataSource dataSource;
    private String metadata;

    public ConnectionInfo mainConnectionInfo() throws AppException {
        Sharding sharding = sharding();
        return new ConnectionInfo(dataSource.getType(), sharding.getMasterHost(), sharding.getMasterPort(),
                sharding.getName(), sharding.getMasterUserName(), DataSourceEncryptUtils.decryptSharding(sharding.masterPassword,sharding.masterHost+sharding.masterUserName));
    }

    public ConnectionInfo backupConnectionInfo() throws AppException {
        Sharding sharding = sharding();
        return new ConnectionInfo(dataSource.getType(), sharding.getSlaveHost(), sharding.getSlavePort(),
                sharding.getName(), sharding.getSlaveUserName(), DataSourceEncryptUtils.decryptSharding(sharding.slavePassword,sharding.slaveHost+sharding.slaveUserName));
    }

    private Sharding sharding() throws AppException {
        List<Sharding> shardings = shardings();
        if (shardings.size() < 1) {
            throw new AppException(ExCode.SYS_002);
        }
        return shardings.get(0);
    }

    public List<Sharding> shardings() throws AppException {
        Objects.requireNonNull(metadata);
        try {
            Map<String, PushSharding> data = objectMapper
                    .readValue(metadata, new TypeReference<ShardMap<String, PushSharding>>() { });

            return data.entrySet().stream().map(Sharding::of).collect(Collectors.toList());
        } catch (IOException | NumberFormatException | ClassCastException | IndexOutOfBoundsException e) {
            throw new AppException(ExCode.SYS_004, e);
        }
    }

    private final static String separator = "#@#";
    public static class ShardMap<K,V>  extends HashMap<K,V> {
        @Override
        public V put(K key, V value) {
            if(value instanceof PushSharding){
                PushSharding pushSharding = (PushSharding)value;
                String tmpKey = (String)key;
                tmpKey = tmpKey +separator+ pushSharding.getMaster();
                K realKey = (K)tmpKey;
                return super.put(realKey, value);
            }else{
                return super.put(key, value);
            }
        }
    }
    /** cobar sharding info **/
    @Data
    public static class Sharding {
        private String name;

        private String masterHost;
        private Integer masterPort;
        private String masterUserName;
        private String masterPassword;

        private String slaveHost;
        private Integer slavePort;
        private String slaveUserName;
        private String slavePassword;

        static Sharding of(Map.Entry<String, PushSharding> entry)
                throws NumberFormatException, IndexOutOfBoundsException {
            PushSharding value = entry.getValue();

            Sharding sharding = new Sharding();
            sharding.setName(entry.getKey().split(separator)[0]);
            List<String> master = Splitter.on(':').splitToList(value.getMaster());
            sharding.setMasterHost(master.get(0));
            sharding.setMasterPort(Integer.parseInt(master.get(1)));
            sharding.setMasterUserName(master.get(2));
            sharding.setMasterPassword(master.get(3));


            List<String> slave = Splitter.on(':').splitToList(value.getSlave());
            sharding.setSlaveHost(slave.get(0));
            sharding.setSlavePort(Integer.parseInt(slave.get(1)));
            sharding.setSlaveUserName(slave.get(2));
            sharding.setSlavePassword(slave.get(3));

            return sharding;
        }
    }

    /** cobar sharding info **/
    @Data
    public static class PushSharding {
        private String master;
        private String slave;
    }
}
