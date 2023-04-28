package com.autodb.ops.dms.web.exception;

/**
 * Redirect Exception
 * @author dongjs
 * @since 2014/7/27
 */
public class RedirectException extends RuntimeException {
    /** redirect url **/
    private String url;

    public RedirectException(String url) {
        super();
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
