package com.autodb.ops.dms.repository.security;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.security.SecurityDataAuth;

import java.util.List;

/**
 * SecurityDataAuth Dao
 *
 * @author dongjs
 * @since 16/1/28
 */
public interface SecurityDataAuthDao {
    void add(SecurityDataAuth securityDataAuth) throws AppException;

    int delete(List<Integer> ids) throws AppException;

    SecurityDataAuth findBySecUser(int secId, String username) throws AppException;

    List<SecurityDataAuth> findAll(Page<SecurityDataAuth> page) throws AppException;

    List<SecurityDataAuth> findByUser(String username, Page<SecurityDataAuth> page) throws AppException;

    List<SecurityDataAuth> findByDsUser(int datasource, String username, Page<SecurityDataAuth> page) throws AppException;
}
