package com.autodb.ops.dms.service.security;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.security.SecurityAuthQuery;
import com.autodb.ops.dms.entity.security.SecurityDataAuth;

import java.util.List;

/**
 * SecurityDataAuth Service
 *
 * @author dongjs
 * @since 16/1/28
 */
public interface SecurityDataAuthService {
    void add(List<Integer> securityIds, List<String> users) throws AppException;

    int delete(List<Integer> ids) throws AppException;

    List<SecurityDataAuth> findByQuery(SecurityAuthQuery query, Page<SecurityDataAuth> page) throws AppException;
}
