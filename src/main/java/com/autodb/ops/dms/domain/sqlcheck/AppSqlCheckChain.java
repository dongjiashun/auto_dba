package com.autodb.ops.dms.domain.sqlcheck;

import com.autodb.ops.dms.entity.datasource.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Application SqlCheckChain
 *
 * @author dongjs
 * @since 2016/12/21
 */
@Component
public class AppSqlCheckChain implements SqlCheckChain {
    private static Logger logger = LoggerFactory.getLogger(AppSqlCheckChain.class);

    private final List<SqlCheck> sqlChecks;

    @Autowired(required = false)
    public AppSqlCheckChain(List<SqlCheck> sqlChecks) {
        if (sqlChecks == null) {
            this.sqlChecks = Collections.emptyList();
        } else {
            this.sqlChecks = sqlChecks;
            this.sqlChecks.sort(Comparator.comparingInt(SqlCheck::getOrder).reversed());
        }
    }

    @Override
    public Optional<String> check(String sql, DataSource dataSource) {
        return sqlChecks.stream()
                .map(sqlCheck -> {
                    try {
                        return sqlCheck.check(sql, dataSource);
                    } catch (Exception e) {
                        logger.warn("sql check error", e);
                        return Optional.<String>empty();
                    }
                })
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
