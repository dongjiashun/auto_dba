package com.autodb.ops.dms.service.sql;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.sql.SqlHistory;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Map;

/**
 * Sql Service
 *
 * @author dongjs
 * @since 16/1/6
 */
public interface SqlService {
    Pair<Boolean, String> formatSql(String sql, String type) throws AppException;

    Pair<Boolean, List<String>> selectStatements(String sql, String type) throws AppException;

    Pair<Boolean, List<String>> queryStatements(String sql, String type) throws AppException;

    /**
     * query
     * @return
     * <ul>
     *   <li>0 -> success</li>
     *   <li>1 -> datasource not find</li>
     *   <li>2 -> bad sql</li>
     * </ul>
     */
    Triple<Integer,Integer,String> query(int userId, int dsId, String sql, boolean record, Page<Map<String, Object>> page) throws AppException;

    /** @see #query(int, int, String, boolean, Page)  **/
    Pair<Integer, String> explain(int userId, int dsId, String sql, Page<Map<String, Object>> page) throws AppException;

    List<SqlHistory> sqlSelectHistory(int userId, Page<SqlHistory> page) throws AppException;

    List<SqlHistory> sqlHistory(int userId, String type, Page<SqlHistory> page) throws AppException;
}
