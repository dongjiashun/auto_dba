package com.autodb.ops.dms.service.sys;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.sys.Broadcast;

import java.util.List;

/**
 * BroadcastService
 *
 * @author dongjs
 * @since 2016/11/9
 */
public interface BroadcastService {
    List<Broadcast> broadcasts() throws AppException;
}
