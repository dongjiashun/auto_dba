package com.autodb.ops.dms.repository.task.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.DataExport;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.DataExportDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * DataExportDao Impl
 * @author dongjs
 * @since 16/1/22
 */
@Repository
public class DataExportDaoImpl extends SuperDao implements DataExportDao {
    @Override
    public void add(DataExport dataExport) throws AppException {
        this.getSqlSession().insert("DataExportMapper.add", dataExport);
    }

    @Override
    public DataExport findByTask(int taskId, String key) throws AppException {
        return this.getSqlSession().selectOne("DataExportMapper.findByTaskAndKey", new HashMap<String, Object>() {
            {
                put("taskId", taskId);
                put("key", key);
            }
        });
    }

    @Override
    public List<DataExport> findByTask(int taskId) throws AppException {
        return this.getSqlSession().selectList("DataExportMapper.findByTask", taskId);
    }

    @Override
    public boolean update(DataExport dataExport) throws AppException {
        return this.getSqlSession().update("DataExportMapper.update", dataExport) > 0;
    }
}
