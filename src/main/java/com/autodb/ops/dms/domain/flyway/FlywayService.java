package com.autodb.ops.dms.domain.flyway;

import com.google.common.base.Splitter;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.task.DataChange;
import com.autodb.ops.dms.entity.task.StructChange;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Flyway Service
 *
 * @author dongjs
 * @since 16/6/22
 */
@Component
public class FlywayService {
    private static final int COMMENT_LINE_LENGTH = 120;

    @Value("${data.file.path}")
    protected String filePath = "";

    protected String migrationsPath() {
        return filePath + "migrations/";
    }

    public void dataChange(DataSource dataSource,  String reason, DataChange dataChange) throws AppException {
        String filename = migrationsPath() + dataSource.getSid() + '/' + dataSource.getEnv()
                + "/V" + getDateVersion() + "__DATA_CHANGE" + ".sql";
        writeFile(filename, getSqlComment(reason) + dataChange.getSql());
    }

    public void structChange(DataSource dataSource, String reason, StructChange structChange) throws AppException {
        String filename = migrationsPath() + dataSource.getSid() + '/' + dataSource.getEnv()
                + "/V" + getDateVersion() + "__STRUCT_CHANGE" + ".sql";
        writeFile(filename, getSqlComment(reason) + structChange.getSql());
    }

    private void writeFile(String filename, String content) throws AppException {
        try {
            File file = new File(filename);
            FileUtils.write(file, content, "UTF-8");
        } catch (IOException e) {
            throw new AppException(ExCode.SYS_005, e);
        }
    }

    private String getDateVersion() {
        return FastDateFormat.getInstance("yyyyMMddHHmmss").format(System.currentTimeMillis());
    }

    private String getSqlComment(String reason) {
        return Splitter.fixedLength(COMMENT_LINE_LENGTH).trimResults()
                .splitToList("# " + reason)
                .stream()
                .collect(Collectors.joining("\n# ")) + "\n\n";
    }
}
