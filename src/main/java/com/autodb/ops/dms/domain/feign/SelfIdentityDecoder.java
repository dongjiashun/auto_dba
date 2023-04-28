package com.autodb.ops.dms.domain.feign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Response;
import feign.Util;
import feign.jackson.JacksonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

public class SelfIdentityDecoder extends JacksonDecoder {
    private static Logger log = LoggerFactory.getLogger(SelfIdentityDecoder.class);

    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (response.status() == 404) return Util.emptyValueOf(type);
        if (response.body() == null) return null;
        Reader reader = response.body().asReader();
        if(type.getTypeName().equalsIgnoreCase("java.lang.String")){
            if (!reader.markSupported()) {
                reader = new BufferedReader(reader, 1);
            }
            try {
                // Read the first byte to see if we have any data
                reader.mark(1);
                if (reader.read() == -1) {
                    return null; // Eagerly returning null avoids "No content to map due to end-of-input"
                }
                reader.reset();
                return ((BufferedReader)reader).readLine();
            } catch (RuntimeJsonMappingException e) {
                if (e.getCause() != null && e.getCause() instanceof IOException) {
                    throw IOException.class.cast(e.getCause());
                }
                throw e;
            }
        }else{
            StringBuffer stringBuffer = new StringBuffer();
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line  = null;
            while((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }
            log.info("从archer获取的内容:\n"+stringBuffer);
            return mapper.readValue(stringBuffer.toString(), mapper.constructType(type));
//            return super.decode(response, type);
        }
    }
}
