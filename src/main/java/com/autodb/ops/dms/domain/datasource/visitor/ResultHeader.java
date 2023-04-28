package com.autodb.ops.dms.domain.datasource.visitor;

import org.apache.commons.lang3.ArrayUtils;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * result header
 *
 * @author dongjs
 * @since 16/1/7
 */
public class ResultHeader {
    private int columnCount;
    private String[] columnNames;
    private int[] columnTypes;
    private String[] columnTypeNames;
    private String[] columnClassNames;
    private String[] columnLabels;

    public static ResultHeader of(ResultSetMetaData metaData) throws SQLException {
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];
        int[] columnTypes = new int[columnCount];
        String[] columnTypeNames = new String[columnCount];
        String[] columnClassNames = new String[columnCount];
        String[] columnLabels = new String[columnCount];

        for (int i = 0; i < columnCount; i++) {
            int index = i + 1;
            columnNames[i] = metaData.getColumnName(index);
            columnTypes[i] = metaData.getColumnType(index);
            columnTypeNames[i] = metaData.getColumnTypeName(index);
            columnClassNames[i] = metaData.getColumnClassName(index);
            columnLabels[i] = metaData.getColumnLabel(index);
        }

        return new ResultHeader(columnCount, columnNames, columnTypes, columnTypeNames, columnClassNames, columnLabels);
    }

    public ResultHeader() {
    }

    public ResultHeader(int columnCount, String[] columnNames, int[] columnTypes,
                        String[] columnTypeNames, String[] columnClassNames, String[] columnLabels) {
        this.columnCount = columnCount;
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
        this.columnTypeNames = columnTypeNames;
        this.columnClassNames = columnClassNames;
        this.columnLabels = columnLabels;
    }

    public void removeColumn(int... indices) {
        for (int index : indices) {
            if (index < 0 || index > columnCount - 1) {
                return;
            }
        }
        columnCount -= indices.length;
        columnNames = ArrayUtils.removeAll(columnNames, indices);
        columnTypes = ArrayUtils.removeAll(columnTypes, indices);
        columnTypeNames = ArrayUtils.removeAll(columnTypeNames, indices);
        columnClassNames = ArrayUtils.removeAll(columnClassNames, indices);
        columnLabels = ArrayUtils.removeAll(columnLabels, indices);
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public int[] getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(int[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    public String[] getColumnTypeNames() {
        return columnTypeNames;
    }

    public void setColumnTypeNames(String[] columnTypeNames) {
        this.columnTypeNames = columnTypeNames;
    }

    public String[] getColumnClassNames() {
        return columnClassNames;
    }

    public void setColumnClassNames(String[] columnClassNames) {
        this.columnClassNames = columnClassNames;
    }

    public String[] getColumnLabels() {
        return columnLabels;
    }

    public void setColumnLabels(String[] columnLabels) {
        this.columnLabels = columnLabels;
    }
}
