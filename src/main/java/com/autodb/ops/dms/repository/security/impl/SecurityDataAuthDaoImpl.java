package com.autodb.ops.dms.repository.security.impl;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.security.SecurityDataAuth;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.security.SecurityDataAuthDao;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * SecurityDataAuthDao Impl
 *
 * @author dongjs
 * @since 16/1/28
 */
@Repository

public class SecurityDataAuthDaoImpl extends SuperDao implements SecurityDataAuthDao {
    @Override
    public void add(SecurityDataAuth securityDataAuth) throws AppException {
        this.getSqlSession().insert("SecurityDataAuthMapper.add", securityDataAuth);
    }

    @Override
    public int delete(List<Integer> ids) throws AppException {
        return this.getSqlSession().delete("SecurityDataAuthMapper.delete", ids);
    }

    @Override
    public SecurityDataAuth findBySecUser(int secId, String username) throws AppException {
        return this.getSqlSession().selectOne("SecurityDataAuthMapper.findBySecUser", new HashMap<String, Object>() {
            {
                put("secId", secId);
                put("username", username);
            }
        });
    }

    @Override
    public List<SecurityDataAuth> findAll(Page<SecurityDataAuth> page) throws AppException {
        page.pagination.setRowCount((int) this.selectCount("SecurityDataAuthMapper.findAll"));
        return this.getSqlSession().selectList("SecurityDataAuthMapper.findAll",
                new RowBounds(page.pagination.getOffset(), page.pagination.getLimit()));
    }

    @Override
    public List<SecurityDataAuth> findByUser(String username, Page<SecurityDataAuth> page) throws AppException {
        page.pagination.setRowCount((int) this.selectCount("SecurityDataAuthMapper.findByUser", username));
        return this.getSqlSession().selectList("SecurityDataAuthMapper.findByUser", username,
                new RowBounds(page.pagination.getOffset(), page.pagination.getLimit()));
    }

    @Override
    public List<SecurityDataAuth> findByDsUser(int datasource, String username, Page<SecurityDataAuth> page) throws AppException {
        HashMap<String, Object> params = new HashMap<String, Object>() {
            {
                put("datasource", datasource);
                put("username", username);
            }
        };

        page.pagination.setRowCount((int) this.selectCount("SecurityDataAuthMapper.findByDsUser", params));
        return this.getSqlSession().selectList("SecurityDataAuthMapper.findByDsUser", params,
                new RowBounds(page.pagination.getOffset(), page.pagination.getLimit()));
    }
}
