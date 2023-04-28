package com.autodb.ops.dms.entity.datasource;

import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.domain.datasource.visitor.ConnectionInfo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * DataSource config
 *
 * @author dongjs
 * @since 2015/12/28
 */
@Data
public class DataSource {
    private Integer id;
    @NotNull
    @Size(min = 1, max = 30)
    private String name;
    @NotNull
    @Size(min = 1, max = 10)
    private String type;
    @NotNull
    @Size(min = 1, max = 10)
    private String env;

    @NotNull
    @Size(min = 1, max = 30)
    private String sid;
    @NotNull
    @Size(min = 1, max = 100)
    private String host;
    @NotNull
    @Range(min = 1, max = 65535)
    private Integer port;
    @NotNull
    @Size(min = 1, max = 30)
    private String username;
    @NotNull
    @Size(min = 1, max = 64)
    private String password;

    @NotNull
    @Size(min = 1, max = 30)
    private String sid2;
    @NotNull
    @Size(min = 1, max = 100)
    private String host2;
    @NotNull
    @Range(min = 1, max = 65535)
    private Integer port2;
    @NotNull
    @Size(min = 1, max = 30)
    private String username2;
    @NotNull
    @Size(min = 1, max = 64)
    private String password2;

    private DataSourceProxy proxy;
    @Range(min = 1, max = 65535)
    private Integer proxyPort;
    @Size(min = 1, max = 30)
    private String proxySid;
    @Size(min = 1, max = 30)
    private String proxyUsername;
    @Size(min = 1, max = 64)
    private String proxyPassword;

    private Date gmtCreate;
    @Null
    private Date gmtModified;

    private boolean cobar;

    public ConnectionInfo mainConnectionInfo() {
        return new ConnectionInfo(type, host, port, sid, username, DataSourceEncryptUtils.getDecryptPassword1(this));
    }

    public ConnectionInfo backupConnectionInfo() {
        return new ConnectionInfo(type, host2, port2, sid2, username2, DataSourceEncryptUtils.getDecryptPassword2(this));
    }

    public ConnectionInfo proxyConnectionInfo() {
        return new ConnectionInfo(type, proxy.getHost(), proxyPort, proxySid, proxyUsername,
                DataSourceEncryptUtils.getDecryptProxyPassword(this));
    }

    /** datasource evn **/
    public static final class Env {
        public static final String PROD = "prod";
        public static final String TEST = "test";
        public static final String DEV = "dev";

        public static String getEnv(String env) {
            String result;
            if (StringUtils.isBlank(env)) {
                result = DataSource.Env.PROD;
            } else {
                switch (env) {
                    case TEST:
                        result = TEST;
                        break;
                    case DEV:
                        result = DEV;
                        break;
                    case PROD:
                    default:
                        result = PROD;
                }
            }

            return result;
        }

        public static String getEnvName(String env) {
            String name;
            switch (env) {
                case "test":
                    name = "测试环境";
                    break;
                case "dev":
                    name = "开发环境";
                    break;
                default:
                    name = "正式环境";
            }

            return name;
        }
    }
}
