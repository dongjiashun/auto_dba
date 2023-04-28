package com.autodb.ops.dms.common.exception;

/**
 * <p>
 * 异常明细，由异常编码和异常信息组成<br/>
 * 异常编码用于确定异常的类型,用于程序处理和用户提示信息<br/>
 * 异常信息一般用于调试和日志,而不是用于提示用户的信息，同Exception的message
 * </p>
 *
 * @author dongjs
 * @since 2012-6-12
 */
public class ExDetail {
    /**
     * 异常编码
     */
    private int code;
    /**
     * 异常信息,一般用于调试和日志,而不是用于提示用户的信息
     */
    private String message;

    public ExDetail(int code) {
        this(code, "");
    }

    public ExDetail(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "[code=" + code + ", message=" + message + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExDetail exDetail = (ExDetail) o;
        return code == exDetail.code && message.equals(exDetail.message);

    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + message.hashCode();
        return result;
    }
}
