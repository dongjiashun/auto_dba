package com.autodb.ops.dms.repository.sys;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.sys.Broadcast;

import java.util.List;

/**
 * Broadcast Dao
 *
 * @author dongjs
 * @since 2016/11/9
 */
public interface BroadcastDao {
    List<Broadcast> broadcasts() throws AppException;
}
