package com.autodb.ops.dms.repository.task.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.TaskBizDao;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * TaskBizDao Impl
 *
 * @author dongjs
 * @since 16/1/18
 */
@Repository
public class TaskBizDaoImpl extends SuperDao implements TaskBizDao {
    @Override
    public void add(TaskBiz taskBiz) throws AppException {
        this.getSqlSession().insert("TaskBizMapper.add", taskBiz);
    }

    @Override
    public boolean updateProcessInstanceId(int id, String processInstanceId) throws AppException {
        return this.getSqlSession().update("TaskBizMapper.updateProcessInstanceId", new HashMap<String, Object>() {
            {
                put("id", id);
                put("processInstanceId", processInstanceId);
            }
        }) > 0;
    }

    @Override
    public TaskBiz findByProcessInstanceId(String processInstanceId) throws AppException {
        return this.getSqlSession().selectOne("TaskBizMapper.findByProcessInstanceId", processInstanceId);
    }

    @Override
    public List<TaskBiz> findByProcessInstanceIds(List<String> processInstanceIds) throws AppException {
        if (processInstanceIds.size() < 1) {
            return Collections.emptyList();
        }
        return this.getSqlSession().selectList("TaskBizMapper.findByProcessInstanceIds", processInstanceIds);
    }

    @Override
    public List<TaskBiz> findByIds(List<Integer> taskIds) throws AppException {
        if (taskIds.size() < 1) {
            return Collections.emptyList();
        }
        return this.getSqlSession().selectList("TaskBizMapper.findByIds", taskIds);
    }

    @Override
    public int update(TaskBiz taskBiz) throws AppException {
        return this.getSqlSession().update("TaskBizMapper.update", taskBiz);
    }
}
