package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.StructChangeStash;

import java.util.List;

/**
 * StructChangeStash Dao
 * @author dongjs
 * @since 2017/1/4
 */
public interface StructChangeStashDao {
    void add(StructChangeStash structChangeStash) throws AppException;

    int deleteByLastId(int dataSourceId, int lastId) throws AppException;

    List<StructChangeStash> findByDatasource(int dataSourceId) throws AppException;
}
