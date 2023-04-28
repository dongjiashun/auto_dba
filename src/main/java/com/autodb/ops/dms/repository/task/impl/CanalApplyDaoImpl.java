package com.autodb.ops.dms.repository.task.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.CanalApply;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.CanalApplyDao;
import org.springframework.stereotype.Repository;

/**
 * CanalApplyDao Impl
 *
 * @author dongjs
 * @since 2016/11/1
 */
@Repository
public class CanalApplyDaoImpl extends SuperDao implements CanalApplyDao {
    @Override
    public void add(CanalApply canalApply) throws AppException {
        this.getSqlSession().insert("CanalApplyMapper.add", canalApply);
    }

    @Override
    public CanalApply findByTask(int taskId) throws AppException {
        return this.getSqlSession().selectOne("CanalApplyMapper.findByTask", taskId);
    }

    @Override
    public boolean update(CanalApply canalApply) throws AppException {
        return this.getSqlSession().update("CanalApplyMapper.update", canalApply) > 0;
    }
}
