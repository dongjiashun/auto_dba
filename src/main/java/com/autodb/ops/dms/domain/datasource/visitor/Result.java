package com.autodb.ops.dms.domain.datasource.visitor;

import java.util.List;
import java.util.Map;

/**
 * Result = ResultHeader + List<Map<String, Object>>
 *
 * @author dongjs
 * @since 16/1/7
 */
public class Result {
    private ResultHeader header;

    private List<Map<String, Object>> data;

    public Result() {
    }

    public Result(ResultHeader header, List<Map<String, Object>> data) {
        this.header = header;
        this.data = data;
    }

    public ResultHeader getHeader() {
        return header;
    }

    public void setHeader(ResultHeader header) {
        this.header = header;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
