package com.autodb.ops.dms.service.sys.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.sys.Broadcast;
import com.autodb.ops.dms.repository.sys.BroadcastDao;
import com.autodb.ops.dms.service.sys.BroadcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BroadcastServiceImpl
 *
 * @author dongjs
 * @since 2016/11/9
 */
@Service
public class BroadcastServiceImpl implements BroadcastService {
    @Autowired
    private BroadcastDao broadcastDao;

    @Override
    public List<Broadcast> broadcasts() throws AppException {
        return broadcastDao.broadcasts();
    }
}
