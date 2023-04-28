package com.autodb.ops.dms.domain.feign;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dianwoba.springboot.webapi.WebApiResponse;
import feign.Response;
import feign.jackson.JacksonDecoder;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * feign WebApi JacksonDecoder
 * @author dongjs
 * @since 16/3/18
 */
public class WebApiJacksonDecoder extends JacksonDecoder {
    private static final String WEB_API_NAME = "com.autodb.springboot.webapi.WebApiResponse";

    public WebApiJacksonDecoder() {
    }

    public WebApiJacksonDecoder(Iterable<Module> modules) {
        super(modules);
    }

    public WebApiJacksonDecoder(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (type.getTypeName().startsWith(WEB_API_NAME)) {
            return super.decode(response, type);
        } else {
            ParameterizedType newType = TypeUtils.parameterize(WebApiResponse.class, type);
            WebApiResponse resp = (WebApiResponse) super.decode(response, newType);
            if (resp.getCode() != WebApiResponse.SUCCESS_CODE) {
                throw new IOException(resp.getError());
            }
            return resp.getData();
        }
    }
}
