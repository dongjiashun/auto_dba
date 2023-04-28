package com.autodb.ops.dms.repository.task.impl;

import com.google.common.collect.ImmutableMap;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.StructChangeStash;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.StructChangeStashDao;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * StructChangeStashDao Impl
 *
 * @author dongjs
 * @since 2017/1/4
 */
@Repository
public class StructChangeStashDaoImpl extends SuperDao implements StructChangeStashDao {
    @Override
    public void add(StructChangeStash structChangeStash) throws AppException {
        this.getSqlSession().insert("StructChangeStashMapper.add", structChangeStash);
    }

    @Override
    public int deleteByLastId(int dataSourceId, int lastId) throws AppException {
        return this.getSqlSession().delete("StructChangeStashMapper.deleteByLastId", ImmutableMap.of("dataSourceId", dataSourceId, "lastId", lastId));
    }

    @Override
    public List<StructChangeStash> findByDatasource(int dataSourceId) throws AppException {
        return this.getSqlSession().selectList("StructChangeStashMapper.findByDatasource", dataSourceId);
    }
}
