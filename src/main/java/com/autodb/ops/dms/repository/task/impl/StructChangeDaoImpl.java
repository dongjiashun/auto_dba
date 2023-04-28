package com.autodb.ops.dms.repository.task.impl;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.datasource.DataSourceOnline;
import com.autodb.ops.dms.entity.task.StructChange;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.StructChangeDao;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StructChangeDao Impl
 *
 * @author dongjs
 * @since 16/5/28
 */
@Repository
public class StructChangeDaoImpl extends SuperDao implements StructChangeDao {
    @Override
    public void add(StructChange structChange) throws AppException {
        this.getSqlSession().insert("StructChangeMapper.add", structChange);
    }

    @Override
    public StructChange findByTask(int taskId, String key) throws AppException {
        return this.getSqlSession().selectOne("StructChangeMapper.findByTaskAndKey", new HashMap<String, Object>() {
            {
                put("taskId", taskId);
                put("key", key);
            }
        });
    }

    @Override
    public StructChange findId(int id) throws AppException{
        return this.getSqlSession().selectOne("StructChangeMapper.findById", new HashMap<String, Object>() {
            {
                put("id", id);
            }
        });
    }

    @Override
    public List<StructChange> findByTask(int taskId) throws AppException {
        return this.getSqlSession().selectList("StructChangeMapper.findByTask", taskId);
    }

    @Override
    public List<StructChange> findByKey(String key) throws AppException {
        return this.getSqlSession().selectList("StructChangeMapper.findByKey", key);
    }

    @Override
    public List<StructChange> findByOnline(String key, DataSourceOnline online, Page<StructChange> page)
            throws AppException {
        Map<String, Object> params = new HashMap<>();
        params.put("key", key);
        params.put("online", online);

        page.pagination.setRowCount((int) this.selectCount("StructChangeMapper.findByOnline", params));
        return this.getSqlSession().selectList("StructChangeMapper.findByOnline", params,
                new RowBounds(page.pagination.getOffset(), page.pagination.getLimit()));
    }

    @Override
    public boolean existInProcessOnline(String key) throws AppException {
        return this.getSqlSession().selectList("StructChangeMapper.inProcessOnline", key).size() > 0;
    }

    @Override
    public List<TaskBiz> inProcessOnline(String key) throws AppException {
        return this.getSqlSession().selectList("StructChangeMapper.inProcessOnline", key);
    }

    @Override
    public boolean update(StructChange dataChange) throws AppException {
        return this.getSqlSession().update("StructChangeMapper.update", dataChange) > 0;
    }

    @Override
    public boolean hasFrequentChange(String user, List<Integer> dsList, int interval) throws AppException {
        if (dsList.isEmpty()) {
            return false;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("user", user);
        params.put("dsList", dsList.stream().map(Object::toString).collect(Collectors.toList()));
        params.put("time", new Date(System.currentTimeMillis() - interval * 60 * 1000));

        return this.getSqlSession().selectList("frequentChange", params).size() > 0;
    }
}
