package com.autodb.ops.dms.domain.canal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.autodb.ops.dms.domain.feign.EncoderExpander;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import lombok.Data;

import java.util.List;

/**
 * CanalService
 * @author dongjs
 * @since 2016/10/26
 */
public interface CanalService {
    @RequestLine("POST /index.php/mysql/ajaxpost")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("ip={ip}&port={port}&charset=utf8")
    Result<String> importDatasource(@Param("ip") String ip, @Param("port") int port);

    @RequestLine("POST /index.php/mysql/del")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("ip={ip}&port={port}")
    Result<String> deleteDatasource(@Param("ip") String ip, @Param("port") int port);

    @RequestLine("POST /index.php/synchronous/focusmanage")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("ip={ip}&port={port}")
    Result<List<Manager>> manager(@Param("ip") String ip, @Param("port") int port);

    @RequestLine("POST /index.php/synchronous/post")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @Body("client_name=kafka&db_ip={ip}&db_port={port}&db_schema={schema}&table_name={table}"
            + "&db_table_idx={index}&tgt_name={target}&tgt_key={key}&manager_id={manager}")
    Result<String> addSync(@Param("ip") String ip, @Param("port") int port,
                           @Param(value = "schema", expander = EncoderExpander.class) String schema,
                           @Param(value = "table", expander = EncoderExpander.class) String table,
                           @Param(value = "target", expander = EncoderExpander.class) String target,
                           @Param("index") int index,
                           @Param(value = "key", expander = EncoderExpander.class) String key,
                           @Param(value = "manager", expander = EncoderExpander.class) int manager);

    /**
     * Result
     * @param <T>
     */
    @Data
    class Result<T> {
        private int code;
        private String error;
        private T data;
    }

    /** Manager **/
    @Data
    class Manager {
        @JsonProperty("manager_id")
        private int id;
        private String ip;
        private int port;
        @JsonProperty("manager")
        private String name;
    }
}
