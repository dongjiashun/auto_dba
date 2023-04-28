package com.autodb.ops.dms.repository.task.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.RuJob;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.RuJobDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class RuJobDaoImpl extends SuperDao implements RuJobDao {
    @Override
    public RuJob findByExecutionId(String execution_id) throws AppException {
        return this.getSqlSession().selectOne("RuJobMapper.findByExecutionId", new HashMap<String, Object>() {
            {
                put("execution_id", execution_id);
            }
        });
    }

    @Override
    public boolean update(RuJob ruJob) throws AppException {
        return this.getSqlSession().update("RuJobMapper.update", ruJob) > 0;
    }
}
