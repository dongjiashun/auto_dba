package com.autodb.ops.dms.repository;

import com.autodb.ops.dms.common.util.MybatisUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * super dao
 * @author dongjs
 * @since 2015/11/10
 */
@Component
public class SuperDao extends SqlSessionDaoSupport {
    @Override
    @Autowired
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    public long selectCount(String statement) {
        return this.selectCount(statement, null);
    }

    public long selectCount(String statement, Object parameter) {
        return this.selectCount(this.getSqlSession(), statement, parameter);
    }

    /**
     * 获取select的总数量<br/>
     * 采用select count(*)
     */
    private long selectCount(SqlSession sqlSession, String statement, Object parameter) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        SqlSession ss = null;
        SqlSessionFactory sf = null;
        try {
            long count = 0L;
            MappedStatement mst = sqlSession.getConfiguration().getMappedStatement(statement);
            // PreparedStatement sql
            BoundSql boundSql = mst.getBoundSql(parameter);
            String sql = "SELECT count(*) total_count FROM (" + boundSql.getSql() + ") tb";

            // Connection conn = sqlSession.getConnection(); // bug get closed connection
            SqlSessionTemplate st = (SqlSessionTemplate) sqlSession;
            sf = st.getSqlSessionFactory();
            ss = SqlSessionUtils.getSqlSession(sf, st.getExecutorType(), st.getPersistenceExceptionTranslator());
            Connection conn = ss.getConnection();

            // log proxy
            if (MybatisUtils.PREPARED_STATEMENT_LOG.isDebugEnabled() || MybatisUtils.CONNECTION_LOG.isDebugEnabled()) {
                conn =  ConnectionLogger.newInstance(conn, MybatisUtils.PREPARED_STATEMENT_LOG, 0);
            }
            stmt = conn.prepareStatement(sql);
            MybatisUtils.setParameters(stmt, mst, boundSql, parameter);
            rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getLong("total_count");
            }
            return count;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(stmt);
            if (null != ss) {
                SqlSessionUtils.closeSqlSession(ss, sf);
            }
        }
    }
}
