package com.autodb.ops.dms.domain.inception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.domain.feign.EncoderExpander;
import com.autodb.ops.dms.entity.datasource.DataSource;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import lombok.Data;

import java.util.List;
import java.util.Optional;

/**
 * Inception Service
 * @author dongjs
 * @since 16/5/26
 */
public interface InceptionService {
    @RequestLine("POST /api/check?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}")
    List<Result> check(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                       @Param("ds") String ds);

    @RequestLine("POST /api/check?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}&env={env}")
    List<Result> check(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                       @Param("ds") String ds,
                       @Param("env") boolean envIsOnline);

    @RequestLine("POST /api/check?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}")
    List<Result> check(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                       @Param(value = "ds", expander = DsExpander.class) DataSource ds);

    @RequestLine("POST /api/check?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}&env={env}")
    List<Result> check(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                       @Param(value = "ds", expander = DsExpander.class) DataSource ds,
                       @Param("env") boolean envIsOnline);

    @RequestLine("POST /api/check?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}&cobar=1&cobar_ds={cobar}")
    List<Result> check(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                                @Param(value = "ds", expander = DsExpander.class) DataSource ds,
                                @Param(value = "cobar", expander = EncoderExpander.class) String cobar);

    @RequestLine("POST /api/check?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}&env={env}&cobar=1&cobar_ds={cobar}")
    List<Result> check(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                       @Param(value = "ds", expander = DsExpander.class) DataSource ds,
                       @Param(value = "cobar", expander = EncoderExpander.class) String cobar,
                       @Param("env") boolean envIsOnline);

    @RequestLine("POST /api/execute?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}")
    List<Result> execute(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                         @Param("ds") String ds);

    @RequestLine("POST /api/execute?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}&env={env}")
    List<Result> execute(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                         @Param("ds") String ds,
                         @Param("env") boolean envIsOnline);

    @RequestLine("POST /api/execute?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}")
    List<Result> execute(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                         @Param(value = "ds", expander = DsExpander.class) DataSource ds);

    @RequestLine("POST /api/execute?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}&env={env}")
    List<Result> execute(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                         @Param(value = "ds", expander = DsExpander.class) DataSource ds,
                         @Param("env") boolean envIsOnline);

    @RequestLine("POST /api/epoll?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}")
    String asyncExecute(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                                @Param("ds") String ds);

    @RequestLine("POST /api/epoll?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}&env={env}")
    String asyncExecute(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                        @Param("ds") String ds,
                        @Param("env") boolean envIsOnline);

    @RequestLine("POST /api/epoll?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}")
    String asyncExecute(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                        @Param(value = "ds", expander = DsExpander.class) DataSource ds);

    @RequestLine("POST /api/epoll?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("archer={sql}&ds={ds}&env={env}")
    List<String> asyncExecute(@Param(value = "sql", expander = EncoderExpander.class) String sql,
                        @Param(value = "ds", expander = DsExpander.class) DataSource ds,
                        @Param("env") boolean envIsOnline);

    @RequestLine("POST /api/progress?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("hash={hash}")
    ProgressResult execProgress(@Param("hash") String hash);

    @RequestLine("POST /api/progress?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("hash={hash}&env={env}")
    ProgressResult execProgress(@Param("hash") String hash,
                                @Param("env") boolean envIsOnline);

    @RequestLine("POST /api/cancelprogress?prettyJson=1")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("hash={hash}&env={env}")
    ProgressResult cancelProgress(@Param("hash") String hash,
                                @Param("env") boolean envIsOnline);

    static Optional<String> getError(List<Result> results) {
        return results.stream()
                .filter(result -> result.getErr() != 0)
                .map(Result::getMessage)
                .findFirst();
    }

    /** result **/
    @Data
    class Result {
        @JsonProperty("errlevel")
        private int err;

        @JsonProperty("SQL")
        private String sql;

        @JsonProperty("errormessage")
        private String message;

        @JsonProperty("Affected_rows")
        private int affectedRows;

        @JsonProperty("execute_time")
        private String executeTime;

        @JsonProperty("stagestatus")
        private String stageStatus;
    }

    /** process result **/
    @Data
    class ProgressResult {
        private int status;

        private String messages;

        @JsonProperty("fin_messages")
        private List<Result> finalMessages;

        /** status **/
        public static class Status {
            public static final int RUNNING = 0;
            public static final int FAIL = 1;
            public static final int SUCCESS = 2;
        }
    }

    /** datasource expander **/
    class DsExpander implements Param.Expander {
        @Override
        public String expand(Object value) {
            if (value instanceof DataSource) {
                DataSource ds = (DataSource) value;
                return ds.getHost() + ':' + ds.getPort() + ':' + ds.getSid() + ':' + ds.getUsername() + ':'
                        + DataSourceEncryptUtils.getDecryptPassword1(ds);
            } else {
                return value.toString();
            }
        }
    }
}
