package com.autodb.ops.dms.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class JSON {
    public static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    public static String objectToString(Object object){
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static  <T> T parseObject(String jsonString, Class<T> tClass){
        try {
            return mapper.readValue(jsonString,tClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
