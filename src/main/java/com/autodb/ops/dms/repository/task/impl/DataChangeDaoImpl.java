package com.autodb.ops.dms.repository.task.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.DataChange;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.DataChangeDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * DataChangeDao Impl
 *
 * @author dongjs
 * @since 16/1/25
 */
@Repository
public class DataChangeDaoImpl extends SuperDao implements DataChangeDao {
    @Override
    public void add(DataChange dataChange) throws AppException {
        this.getSqlSession().insert("DataChangeMapper.add", dataChange);
    }

    @Override
    public DataChange findByTask(int taskId, String key) throws AppException {
        return this.getSqlSession().selectOne("DataChangeMapper.findByTaskAndKey", new HashMap<String, Object>() {
            {
                put("taskId", taskId);
                put("key", key);
            }
        });
    }

    @Override
    public List<DataChange> findByTask(int taskId) throws AppException {
        return this.getSqlSession().selectList("DataChangeMapper.findByTask", taskId);
    }

    @Override
    public boolean update(DataChange dataChange) throws AppException {
        return this.getSqlSession().update("DataChangeMapper.update", dataChange) > 0;
    }
}
