package com.autodb.ops.dms.repository.task.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.SchemaApply;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.task.SchemaApplyDao;
import org.springframework.stereotype.Repository;

/**
 * SchemaApplyDao Impl
 *
 * @author dongjs
 * @since 16/7/26
 */
@Repository
public class SchemaApplyDaoImpl extends SuperDao implements SchemaApplyDao {
    @Override
    public void add(SchemaApply schemaApply) throws AppException {
        this.getSqlSession().insert("SchemaApplyMapper.add", schemaApply);
    }

    @Override
    public SchemaApply findByTask(int taskId) throws AppException {
        return this.getSqlSession().selectOne("SchemaApplyMapper.findByTask", taskId);
    }

    @Override
    public boolean update(SchemaApply schemaApply) throws AppException {
        return this.getSqlSession().update("SchemaApplyMapper.update", schemaApply) > 0;
    }
}
