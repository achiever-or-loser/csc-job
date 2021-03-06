package com.csc.job.core.biz.model;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz.model
 * @Author: 陈世超
 * @Create: 2020-10-12 14:32
 * @Version: 1.0
 */
public class ReturnT<T> {
    public static final long serialVersionUID = 42L;
    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;

    public static final ReturnT<String> SUCCESS = new ReturnT<String>(null);
    public static final ReturnT<String> FAIL = new ReturnT<>(FAIL_CODE,null);

    private int code;
    private String message;
    private T content;

    public ReturnT() {
    }

    public ReturnT(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ReturnT(T content) {
        this.code = SUCCESS_CODE;
        this.content = content;
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

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ReturnT[" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", content=" + content +
                ']';
    }
}
