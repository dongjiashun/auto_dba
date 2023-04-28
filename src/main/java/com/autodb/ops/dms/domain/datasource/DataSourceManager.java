package com.autodb.ops.dms.domain.datasource;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.domain.datasource.mysql.MySQLDatabaseVisitor;
import com.autodb.ops.dms.domain.datasource.mysql.MySQLSQLService;
import com.autodb.ops.dms.domain.datasource.sql.AbstractSQLService;
import com.autodb.ops.dms.domain.datasource.sql.SQLService;
import com.autodb.ops.dms.domain.datasource.visitor.ConnectionInfo;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataSource Manager
 *
 * @author dongjs
 * @since 2015/12/30
 */
@Component
public class DataSourceManager {
    private static Logger log = LoggerFactory.getLogger(DataSourceManager.class);

    /** unique mark -> visitor **/
    private Map<String, DatabaseVisitor> visitorMap = new ConcurrentHashMap<>();

    // checkActive
    @Scheduled(initialDelay = 3600000, fixedRate = 3600000)
    public synchronized void checkActive() {
        int count = 0;
        Iterator<Map.Entry<String, DatabaseVisitor>> iterator = visitorMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DatabaseVisitor> entry = iterator.next();
            DatabaseVisitor visitor = entry.getValue();
            if (!visitor.isActive()) {
                count++;
                visitor.close();
                iterator.remove();
            }
        }

        if (count > 0) {
            log.info("{} not active DatabaseVisitor closed", count);
        }
    }

    @PreDestroy
    public synchronized void destroy() {
        visitorMap.values().forEach(DatabaseVisitor::close);
    }

    public DatabaseVisitor getDatabaseVisitor(ConnectionInfo info) throws AppException {
        return getDatabaseVisitor(info, true, true);
    }

    public DatabaseVisitor getDatabaseVisitor(ConnectionInfo info, boolean autoCreate) throws AppException {
        return getDatabaseVisitor(info, autoCreate, true);
    }

    public DatabaseVisitor getTempDatabaseVisitor(ConnectionInfo info) throws AppException {
        return getDatabaseVisitor(info, true, false);
    }

    private DatabaseVisitor getDatabaseVisitor(ConnectionInfo info,
                                               boolean autoCreate, boolean autoInit) throws AppException {
        try {
            DatabaseVisitor visitor = visitorMap.get(info.uniqueMark());

            if (visitor == null && autoCreate) {
                visitor = initDatabaseVisitor(info, autoInit);
            }

            return visitor;
        } catch (SQLException e) {
            throw new AppException(ExCode.DS_001, e);
        }
    }

    public SQLService getSQLService(ConnectionInfo info) {
        AbstractSQLService service;
        if ("MYSQL".equalsIgnoreCase(info.getType())) {
            service = new MySQLSQLService();
        } else {
            throw new IllegalArgumentException("unknown database type");
        }
        return service;
    }


    private synchronized DatabaseVisitor initDatabaseVisitor(ConnectionInfo info, boolean autoInit)
            throws SQLException {
        DatabaseVisitor visitor = visitorMap.get(info.uniqueMark());
        if (visitor == null) {
            visitor = createDatabaseVisitor(info);
            if (autoInit) {
                visitor.init(info);
                visitorMap.put(info.uniqueMark(), visitor);
            }
        }
        return visitor;
    }

    private DatabaseVisitor createDatabaseVisitor(ConnectionInfo info) {
        DatabaseVisitor visitor;
        if ("MYSQL".equalsIgnoreCase(info.getType())) {
            visitor = new MySQLDatabaseVisitor();
        } else {
            throw new IllegalArgumentException("unknown database type");
        }
        return visitor;
    }
}
