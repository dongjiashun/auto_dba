package com.autodb.ops.dms.service.sql.impl;

import com.autodb.ops.dms.common.AppContext;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.data.pagination.Pagination;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.util.SqlUtils;
import com.autodb.ops.dms.domain.bi.EncryptionService;
import com.autodb.ops.dms.domain.datasource.DataSourceManager;
import com.autodb.ops.dms.domain.datasource.sql.SQLService;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.domain.datasource.visitor.Result;
import com.autodb.ops.dms.domain.datasource.visitor.ResultHeader;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.sql.SqlHistory;
import com.autodb.ops.dms.entity.user.OperateLog;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.sql.SqlHistoryDao;
import com.autodb.ops.dms.repository.user.OperateLogDao;
import com.autodb.ops.dms.repository.user.UserDao;
import com.autodb.ops.dms.service.security.SecurityDataService;
import com.autodb.ops.dms.service.sql.SqlService;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SqlService Impl
 * @author dongjs
 * @since 16/1/6
 */
@Service
public class SqlServiceImpl implements SqlService {
    private static Logger logger = LoggerFactory.getLogger(SqlServiceImpl.class);

    @Value("${data.select.max-size}")
    private int selectMaxSize = 1000;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private SecurityDataService securityDataService;

    @Autowired
    private SqlHistoryDao sqlHistoryDao;

    @Autowired
    private OperateLogDao operateLogDao;

    @Autowired
    private EncryptionService encryptionService;

    @Override
    public Pair<Boolean, String> formatSql(String sql, String type) throws AppException {
        return SqlUtils.format(sql, type);
    }

    @Override
    public Pair<Boolean, List<String>> selectStatements(String sql, String type) throws AppException {
        return SqlUtils.selectStatements(sql, type);
    }

    @Override
    public Pair<Boolean, List<String>> queryStatements(String sql, String type) throws AppException {
        return SqlUtils.queryStatements(sql, type);
    }

    @Override
    public Triple<Integer,Integer,String> query(int userId, int dsId, String sql, boolean record,
                                       Page<Map<String, Object>> page) throws AppException {
        Triple<Integer,Integer, String> result;
        int costTime = 0;
        try {
            sql = addLimitOrChangSize(sql);
            DataSource dataSource = this.dataSourceDao.findByUser(userId, dsId);
            User user = userDao.find(userId);
            if (null != dataSource && null != user) {
                Pair<Boolean, String> pair = SqlUtils.queryStatement(sql, dataSource.getType());
                if (pair.getLeft()) {
                    sql = pair.getRight();

                    DatabaseVisitor visitor = this.dataSourceManager.getDatabaseVisitor(dataSource.backupConnectionInfo());
                    Result queryResult;
                    if (SqlUtils.selectStatement(sql, dataSource.getType()).getLeft()) {
                        // select
                        // operate log
                        String logStr = "在 " + dataSource.getName() + " 数据库下进行sql查询操作\n" + sql;
                        OperateLog operateLog = OperateLog.of(dataSource, OperateLog.Type.SQL_QUERY, sql);
                        logger.info(user.getUsername() + logStr);
                        operateLogDao.add(operateLog);

                        // 脱敏
                        Set<String> tableSet = new HashSet<>();
                        Map<String, Set<String>> maskData = securityDataService.findMaskData(dataSource.getId(), user.getUsername());
                        SQLService sqlService = this.dataSourceManager.getSQLService(dataSource.backupConnectionInfo());
                        String newSql = sqlService.securityMaskSql(sql, maskData, visitor, tableSet).trim();

                        // count
                        int count = (int) visitor.queryCount(newSql);

                        // query
                        long timeStart = System.currentTimeMillis();
                        queryResult = visitor.query(newSql, page.pagination.getOffset(), page.pagination.getLimit());
                        // Result queryResult = visitor.query(sql, page.pagination.getOffset(), page.pagination.getLimit());
                        long timeEnd = System.currentTimeMillis();
                        int cost = (int) (timeEnd - timeStart);
                        costTime = cost;
                        logger.debug("cost {} ms query sql: {}", cost, sql);

                        // hack for bigint
                        convertLong2String(queryResult);
                        if (record) {
                            SqlHistory sqlHistory = new SqlHistory();
                            sqlHistory.setDataSource(dataSource);
                            sqlHistory.setUser(user);
                            sqlHistory.setType(SqlHistory.Type.SELECT);
                            sqlHistory.setSql(sql);
                            sqlHistory.setExecSql(newSql);
                            sqlHistory.setExecTime(cost);
                            sqlHistory.setCount(count);
                            sqlHistory.setGmtCreate(new Date());
                            this.sqlHistoryDao.add(sqlHistory);
                        }

                        encryptionService.encryptResult(queryResult, AppContext.getCurrentUser().getUsername());

                        page.pagination.setRowCount(count);
                    } else {
                        // other query
                        queryResult = visitor.query(sql);
                        page.pagination.setPageSize(Pagination.MAX_PAGE_SIZE);
                        page.pagination.setRowCount(queryResult.getData().size());
                    }

                    page.setData(queryResult.getData());
                    page.setHeader(queryResult.getHeader());
                    result = Triple.of(0,costTime, null);
                } else {
                    result = Triple.of(2,costTime, pair.getRight());
                }
            } else {
                result = Triple.of(1, costTime,"datasource not find");
            }
        } catch (SQLException e) {
            result = Triple.of(3,costTime, e.getMessage());
        }

        return result;
    }

    public String addLimitOrChangSize(String sql){
        sql = sql.toLowerCase();
        if(!sql.contains("select"))
            return sql;
        if(!sql.contains("limit")){
            boolean hasEndChar = sql.endsWith(";");
            if(hasEndChar){
                sql = sql.replace(";","");
            }
            sql = sql + " limit "+ selectMaxSize;
        }else{//修改默认的limit值，如果maxSize大于selectMaxSize
            int beginIndex = sql.indexOf("limit");//begin inclusive
            String limitString = sql.substring(beginIndex);
            String prefixString = sql.replace(limitString,"");
            limitString = limitString.replaceAll(";","");
            limitString = limitString.replace(","," ");//如果是这种格式: limit 10,10;
            String[] items = limitString.split("\\s+");
            if(items.length == 3){//这种格式: limit 10,10;
                String maxString = items[2];
                try{
                    if(Long.parseLong(maxString) > selectMaxSize){
                        sql = prefixString + " "+items[0] +" "+ items[1]+" , "+selectMaxSize;
                    }
                }catch (Exception epx){//如果输入的数字大于long.maxSize，使用默认的selectMaxSize
                    logger.warn("解析Long异常,maxString={}",maxString);
                    sql = prefixString + " "+items[0] +" "+ items[1]+" , "+selectMaxSize;
                }

            }else if(items.length == 2){//这种格式: limit 10;
                String maxString = items[1];
                try{
                    if(Long.parseLong(maxString) > selectMaxSize){
                        sql = prefixString + " "+items[0] + " " + selectMaxSize;
                    }
                }catch (Exception exp){//如果输入的数字大于long.maxSize，使用默认的selectMaxSize
                    logger.warn("解析Long异常,maxString={}",maxString);
                    sql = prefixString + " "+items[0] + " " + selectMaxSize;
                }

            }
        }
        return sql;
    }

    @Override
    public Pair<Integer, String> explain(int userId, int dsId, String sql, Page<Map<String, Object>> page) throws AppException {
        Pair<Integer, String> result;
        try {
            DataSource dataSource = this.dataSourceDao.findByUser(userId, dsId);
            User user = userDao.find(userId);
            if (null != dataSource && null != user) {
                Pair<Boolean, String> pair = SqlUtils.selectStatement(sql, dataSource.getType());
                if (pair.getLeft()) {
                    sql = pair.getRight();
                    DatabaseVisitor visitor = this.dataSourceManager.getDatabaseVisitor(dataSource.backupConnectionInfo());

                    // query
                    long timeStart = System.currentTimeMillis();
                    Result queryResult = visitor.query("EXPLAIN " + sql);
                    long timeEnd = System.currentTimeMillis();
                    int cost = (int) (timeEnd - timeStart);
                    logger.debug("cost {} ms explain sql: {}", cost, sql);

                    page.pagination.setPageSize(Pagination.MAX_PAGE_SIZE);
                    page.pagination.setRowCount(queryResult.getData().size());
                    page.setData(queryResult.getData());
                    page.setHeader(queryResult.getHeader());
                    result = Pair.of(0, null);
                } else {
                    result = Pair.of(2, pair.getRight());
                }
            } else {
                result = Pair.of(1, "datasource not find");
            }
        } catch (SQLException e) {
            result = Pair.of(3, e.getMessage());
        }

        return result;
    }

    private void convertLong2String(Result result) {
        ResultHeader header = result.getHeader();
        List<Map<String, Object>> data = result.getData();
        if (header == null || data == null || data.size() < 1) {
            return;
        }

        Set<String> longNames = new HashSet<>();
        for (int i = 0; i < header.getColumnClassNames().length; i++) {
            String className = header.getColumnClassNames()[i];
            if ("java.lang.Long".equalsIgnoreCase(className)
                    || "java.math.BigInteger".equalsIgnoreCase(className)) {
                longNames.add(header.getColumnLabels()[i]);
            }
        }

        data.forEach(map -> longNames.forEach(name -> {
            Object value = map.get(name);
            if (value != null) {
                map.put(name, value.toString());
            }
        }));
    }

    @Override
    public List<SqlHistory> sqlSelectHistory(int userId, Page<SqlHistory> page) throws AppException {
        return this.sqlHistory(userId, SqlHistory.Type.SELECT, page);
    }

    @Override
    public List<SqlHistory> sqlHistory(int userId, String type, Page<SqlHistory> page) throws AppException {
        List<SqlHistory> sqlHistories = this.sqlHistoryDao.findByUserType(userId, type, page);
        page.setData(sqlHistories);
        injectDataSource(sqlHistories);
        return sqlHistories;
    }

    private void injectDataSource(List<SqlHistory> sqlList) {
        if (sqlList.size() > 0) {
            List<Integer> dsIds = sqlList.stream().map(sql -> sql.getDataSource().getId()).collect(Collectors.toList());
            Map<Integer, DataSource> dataSourceMap = dataSourceDao.findMap(dsIds);

            sqlList.forEach(sql -> {
                DataSource dataSource = dataSourceMap.get(sql.getDataSource().getId());
                if (dataSource != null) {
                    sql.setDataSource(dataSource);
                }
            });
        }
    }
}
