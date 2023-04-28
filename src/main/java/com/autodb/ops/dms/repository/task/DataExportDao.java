package com.autodb.ops.dms.repository.task;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.task.DataExport;

import java.util.List;

/**
 * DataExport Dao
 *
 * @author dongjs
 * @since 16/1/22
 */
public interface DataExportDao {
    void add(DataExport dataExport) throws AppException;

    DataExport findByTask(int taskId, String key) throws AppException;

    List<DataExport> findByTask(int taskId) throws AppException;

    boolean update(DataExport dataExport) throws AppException;
}
