package com.autodb.ops.dms.domain.datasource.visitor;

import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Header + MapListHandler<br/>
 * This class is not thread safe.
 *
 * @author dongjs
 * @since 16/1/7
 */
public class HeaderMapListHandler extends MapListHandler {
    private ResultHeader resultHeader;

    public HeaderMapListHandler() {
    }

    public HeaderMapListHandler(RowProcessor convert) {
        super(convert);
    }

    @Override
    public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
        handleMetaData(rs.getMetaData());
        return super.handle(rs);
    }

    @Override
    protected Map<String, Object> handleRow(ResultSet rs) throws SQLException {
        return super.handleRow(rs);
    }

    protected void handleMetaData(ResultSetMetaData metaData) throws SQLException {
        this.resultHeader = ResultHeader.of(metaData);
    }

    public ResultHeader getResultHeader() {
        return resultHeader;
    }
}
