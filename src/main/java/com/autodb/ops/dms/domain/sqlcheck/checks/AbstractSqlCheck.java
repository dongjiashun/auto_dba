package com.autodb.ops.dms.domain.sqlcheck.checks;

import com.autodb.ops.dms.domain.sqlcheck.SqlCheck;

/**
 * Abstract SqlCheck
 *
 * @author dongjs
 * @since 2016/12/21
 */
public abstract class AbstractSqlCheck implements SqlCheck {
    @Override
    public int getOrder() {
        return SqlCheck.LOWEST_PRECEDENCE;
    }
}
