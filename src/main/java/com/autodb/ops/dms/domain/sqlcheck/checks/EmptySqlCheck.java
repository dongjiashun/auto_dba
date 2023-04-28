package com.autodb.ops.dms.domain.sqlcheck.checks;

import com.autodb.ops.dms.entity.datasource.DataSource;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Empty SqlCheck
 *
 * @author dongjs
 * @since 2016/12/21
 */
@Component
public class EmptySqlCheck extends AbstractSqlCheck {
    @Override
    public Optional<String> check(String sql, DataSource dataSource) {
        // do nothing
        return Optional.empty();
        // return Optional.of("reason");
    }
}
