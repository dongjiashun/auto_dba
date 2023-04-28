package com.autodb.ops.dms.repository.task.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.DsApply;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.DsApplyDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * DsApplyDao Impl
 *
 * @author dongjs
 * @since 16/1/18
 */
@Repository
public class DsApplyDaoImpl extends SuperDao implements DsApplyDao {
    @Override
    public void add(DsApply dsApply) throws AppException {
        this.getSqlSession().insert("DsApplyMapper.add", dsApply);
    }

    @Override
    public DsApply findByTask(int taskId, String key) throws AppException {
        return this.getSqlSession().selectOne("DsApplyMapper.findByTaskAndKey", new HashMap<String, Object>() {
            {
                put("taskId", taskId);
                put("key", key);
            }
        });
    }

    @Override
    public List<DsApply> findByTask(int taskId) throws AppException {
        return this.getSqlSession().selectList("DsApplyMapper.findByTask", taskId);
    }

    @Override
    public boolean update(DsApply dsApply) throws AppException {
        return this.getSqlSession().update("DsApplyMapper.update", dsApply) > 0;
    }
}
