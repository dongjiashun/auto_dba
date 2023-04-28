package com.autodb.ops.dms.web.exception;

/**
 * PageNotFound Exception
 * @author xieg
 * @since 2014/6/23
 */
public class PageNotFoundException extends RuntimeException {
    public PageNotFoundException(String message) {
        super(message);
    }

    public PageNotFoundException() {
    }
}