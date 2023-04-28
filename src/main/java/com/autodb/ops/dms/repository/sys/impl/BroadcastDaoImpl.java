package com.autodb.ops.dms.repository.sys.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.sys.Broadcast;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.sys.BroadcastDao;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BroadcastDaoImpl
 *
 * @author dongjs
 * @since 2016/11/9
 */
@Repository
public class BroadcastDaoImpl extends SuperDao implements BroadcastDao {
    @Override
    public List<Broadcast> broadcasts() throws AppException {
        return this.getSqlSession().selectList("BroadcastMapper.broadcasts");
    }
}
