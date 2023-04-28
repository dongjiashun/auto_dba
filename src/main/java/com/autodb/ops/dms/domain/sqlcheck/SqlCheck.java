package com.autodb.ops.dms.domain.sqlcheck;

import com.autodb.ops.dms.entity.datasource.DataSource;

import java.util.Optional;

/**
 * SqlCheck
 *
 * @author dongjs
 * @since 2016/12/21
 */
public interface SqlCheck {
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * sql check
     * @return check error
     */
    Optional<String> check(String sql, DataSource dataSource);

    /** Get the order value of this check **/
    int getOrder();
}
