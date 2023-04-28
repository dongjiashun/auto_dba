package com.autodb.ops.dms.domain.sqlcheck;

import com.autodb.ops.dms.entity.datasource.DataSource;

import java.util.Optional;

/**
 * SqlCheck Chain
 *
 * @author dongjs
 * @since 2016/12/21
 */
public interface SqlCheckChain {
    /**
     * sql chain check
     * @return check error
     */
    Optional<String> check(String sql, DataSource dataSource);
}

