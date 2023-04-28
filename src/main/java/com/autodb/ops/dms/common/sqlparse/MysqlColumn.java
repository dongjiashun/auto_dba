package com.autodb.ops.dms.common.sqlparse;

/**
 * Created by wuying on 17/1/13.
 */
public class MysqlColumn {
    private  String name;
    private  String comment;
    private  String dataType;

    public MysqlColumn(String name, String comment, String dataType) {
        this.name = name;
        this.comment = comment;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }



    @Override
    public String toString() {
        return "MysqlColumn{" +
                "name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", dataType='" + dataType + '\'' +
                '}'+"\n";
    }
}
