package com.autodb.ops.dms.common.mybatis.interceptor;

import com.autodb.ops.dms.common.data.dialect.Dialect;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.SimpleStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;


/**
 * <p>
 * mybatis 分页拦截器<br/>
 * 透明实现mybatis 的真正数据库分页<br/>
 * prepare和parameterize是在两个不同点调用的,一个负责生成Statement，一个负责设置参数
 * </p>
 * <p>
 * <p>
 * 配置：
 * <pre>
 * &lt;plugins&gt;
 * 	&lt;plugin interceptor="pkg.PaginationInterceptor"&gt;
 * 		&lt;property name="dialect" value="mysql"/&gt;
 * 	&lt;/plugin&gt;
 * &lt;/plugins&gt;
 * </pre>
 * </p>
 *
 * @author dongjs
 * @since 2011-11-21
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class}),
        @Signature(type = StatementHandler.class, method = "parameterize", args = {Statement.class})})
public class PaginationInterceptor extends AbstractStatementHandlerInterceptor {
    /**
     * Properties中方言的名称
     */
    protected static final String DIALECT = "dialect";

    /**
     * 数据库方言
     */
    protected Dialect dialect;

    /**
     * 配置
     */
    protected Properties properties;

    /* (non-Javadoc)
     * @see org.apache.ibatis.plugin.Interceptor#intercept(org.apache.ibatis.plugin.Invocation)
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method m = invocation.getMethod();
        if ("prepare".equals(m.getName())) {
            return prepare(invocation);
        } else if ("parameterize".equals(m.getName())) {
            return parameterize(invocation);
        }
        return invocation.proceed();
    }

    /**
     * 负责生成Statement
     */
    private Object prepare(Invocation invocation) throws Throwable {
        StatementHandler statement = getStatementHandler(invocation);

        if (statement instanceof SimpleStatementHandler || statement instanceof PreparedStatementHandler) {
            RowBounds rowBounds = getRowBounds(statement);
            if (hasBounds(rowBounds)) {
                BoundSql boundSql = statement.getBoundSql();
                String sql = boundSql.getSql();

                if (statement instanceof SimpleStatementHandler) {
                    // 直接sql
                    sql = dialect.getLimitString(sql, rowBounds.getOffset(), rowBounds.getLimit());
                } else {
                    // 预处理sql
                    sql = dialect.getLimitString(sql, rowBounds.getOffset() > 0);
                }
                FieldUtils.writeField(boundSql, "sql", sql, true);
            }
        }
        return invocation.proceed();
    }

    /**
     * 负责设置参数
     */
    private Object parameterize(Invocation invocation) throws Throwable {
        Statement statement = (Statement) invocation.getArgs()[0];
        Object rtn = invocation.proceed();
        if (statement instanceof PreparedStatement) {
            PreparedStatement ps = (PreparedStatement) statement;
            StatementHandler statementHandler = getStatementHandler(invocation);
            RowBounds rowBounds = getRowBounds(statementHandler);

            if (hasBounds(rowBounds)) {
                BoundSql boundSql = statementHandler.getBoundSql();
                int parameterSize = boundSql.getParameterMappings().size();
                dialect.setLimitParameters(ps, parameterSize, rowBounds.getOffset(), rowBounds.getLimit());

                // 使默认的逻辑分页失效
                setRowBounds(rowBounds, RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);
            }
        }
        return rtn;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;

        String dialectStr = this.properties.getProperty(DIALECT);
        this.dialect = Dialect.getInstance(dialectStr);
        if (!this.dialect.supportsLimit()) {
            throw new RuntimeException(dialectStr + "dialect not support pagination");
        }
    }

}
