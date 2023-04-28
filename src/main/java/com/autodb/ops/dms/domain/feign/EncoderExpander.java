package com.autodb.ops.dms.domain.feign;

import feign.Param;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Encoder Expander
 * @author dongjs
 * @since 16/5/26
 */
public class EncoderExpander implements Param.Expander {
    @Override
    public String expand(Object value) {
        try {
            return URLEncoder.encode(value.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value.toString();
        }
    }
}
