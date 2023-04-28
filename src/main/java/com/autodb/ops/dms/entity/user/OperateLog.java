package com.autodb.ops.dms.entity.user;

import com.autodb.ops.dms.common.DmsWebContext;
import com.autodb.ops.dms.entity.datasource.DataSource;
import lombok.Data;

import java.util.Date;

/**
 * User Operate Log
 *
 * @author dongjs
 * @since 16/4/26
 */
@Data
public class OperateLog {
    private int id;
    private String env;
    private String sid;
    private String operator;
    private int type;
    private String typeString;
    private String ip;
    private String content;
    private Date time;

    public static OperateLog of(String env, String sid, Type type, String operator, String ip, String content) {
        OperateLog log = new OperateLog();
        log.setEnv(DataSource.Env.getEnv(env));
        log.setSid(sid);
        log.type(type);
        log.setOperator(operator);
        log.setIp(ip);
        log.setContent(content);
        log.setTime(new Date());
        return log;
    }

    public static OperateLog of(String env, String sid, Type type, String content) {
        DmsWebContext webContext = DmsWebContext.get();
        return of(env, sid, type, webContext.getUsername(), webContext.getIp(), content);
    }

    public static OperateLog of(DataSource ds, Type type, String content) {
        DmsWebContext webContext = DmsWebContext.get();
        return of(ds.getEnv(), ds.getSid(), type, webContext.getUser().getNickname(), webContext.getIp(), content);
    }

    public void setType(int type) {
        this.type = type;
    }

    public void type(Type type) {
        this.type = type.getType();
        this.typeString = type.getTypeString();
    }

    /**
     * OperateLog type
     **/
    public enum Type {
        DS_ADD(1, "新增数据源"),
        DS_UPDATE(2, "修改数据源"),
        DS_DELETE(3, "删除数据源"),
        SQL_QUERY(4, "SQL查询"),
        STRUCT_QUERY(5, "表结构查询");

        private int type;
        private String typeString;

        Type(int type, String typeString) {
            this.type = type;
            this.typeString = typeString;
        }

        public int getType() {
            return type;
        }

        public String getTypeString() {
            return typeString;
        }
    }
}
