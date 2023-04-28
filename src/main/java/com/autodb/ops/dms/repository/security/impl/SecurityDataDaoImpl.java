package com.autodb.ops.dms.repository.security.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.security.SecurityData;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.security.SecurityDataDao;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * SecurityDataDao Impl
 *
 * @author dongjs
 * @since 16/1/28
 */
@Repository
public class SecurityDataDaoImpl extends SuperDao implements SecurityDataDao {
    @Override
    public List<SecurityData> findByIds(List<Integer> ids) throws AppException {
        if (ids.size() == 0) {
            return Collections.emptyList();
        }
        return this.getSqlSession().selectList("SecurityDataMapper.findByIds", ids);
    }

    @Override
    public List<SecurityData> find(int datasource, String table) throws AppException {
        return this.getSqlSession().selectList("SecurityDataMapper.find", new HashMap<String, Object>() {
            {
                put("datasource", datasource);
                put("table", table);
            }
        });
    }

    @Override
    public List<SecurityData> findNoAuth(int datasource, String username) throws AppException {
        return this.getSqlSession().selectList("SecurityDataMapper.findNoAuth", new HashMap<String, Object>() {
            {
                put("datasource", datasource);
                put("username", username);
            }
        });
    }

    @Override
    public List<String> findTablesByDatasource(int datasource) throws AppException {
        return this.getSqlSession().selectList("SecurityDataMapper.findTablesByDatasource", datasource);
    }

    @Override
    public void add(List<SecurityData> data) throws AppException {
        if (data.size() < 1) {
            return;
        }
        this.getSqlSession().insert("SecurityDataMapper.add", data);
    }

    @Override
    public int delete(int datasource, String table) throws AppException {
        return this.getSqlSession().delete("SecurityDataMapper.delete", new HashMap<String, Object>() {
            {
                put("datasource", datasource);
                put("table", table);
            }
        });
    }

    @Override
    public int deleteAll() throws AppException {
        return this.getSqlSession().delete("SecurityDataMapper.deleteAll");
    }
}
