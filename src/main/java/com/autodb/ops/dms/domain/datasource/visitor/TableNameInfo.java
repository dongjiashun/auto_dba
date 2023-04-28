package com.autodb.ops.dms.domain.datasource.visitor;

import com.alibaba.druid.sql.ast.statement.SQLSelect;

/**
 * TableNameInfo
 */
public class TableNameInfo {
    private String name;
    private String alias;
    private boolean isNestQuery;
    private SQLSelect select;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isNestQuery() {
        return isNestQuery;
    }

    public void setNestQuery(boolean isNestQuery) {
        this.isNestQuery = isNestQuery;
    }

    public SQLSelect getSelect() {
        return select;
    }

    public void setSelect(SQLSelect select) {
        this.select = select;
    }
}