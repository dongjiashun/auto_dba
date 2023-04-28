package com.autodb.ops.dms.domain.datasource.visitor;

import com.google.common.base.Joiner;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Database connection info
 * @author dongjs
 * @since 2015/12/28
 */
@Data
@EqualsAndHashCode(of = { "type", "host", "port", "database", "username", "password" })
public class ConnectionInfo {
    public static final int POOL_INIT_SIZE = 0;
    public static final int POOL_MIN_SIZE = 0;
    public static final int POOL_MAX_SIZE = 10;
    public static final int POOL_MAX_WAIT = 10000;
    public static final int POOL_CONNECT_TIMEOUT = 45000;

    private final String type;
    private final String host;
    private final Integer port;
    private final String database;
    private final String username;
    private final String password;

    private final String mark;

    private String driver;
    private String connectionUrl;
    private String validationQuery;
    private String connectionProperties;

    public ConnectionInfo(String type, String host, int port, String database, String username, String password) {
        super();
        this.type = type;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;

        this.mark = Joiner.on('-').join(type, host, port, database, username, password);
    }

    public String uniqueMark() {
        return mark;
    }
}
