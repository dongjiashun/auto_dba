package com.autodb.ops.dms.repository.task.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.RuJob;
import com.autodb.ops.dms.entity.task.RuTask;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.RuJobDao;
import com.autodb.ops.dms.repository.task.RuTaskDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public class RuTaskDaoImpl extends SuperDao implements RuTaskDao {

    @Override
    public List<RuTask> findByTaskDefKey(String task_def_key) throws AppException {
        return this.getSqlSession().selectList("RuTaskMapper.findByTaskDefKey", new HashMap<String, Object>() {
            {
                put("task_def_key", task_def_key);
            }
        });
    }
}
