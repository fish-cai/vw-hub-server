package com.fish.vwhub.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public final class ResResult<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String code;
    private String message;
    private T data;

    public ResResult() {
        this.success = true;
    }

    public ResResult(String code, String message) {
        this(true, code, message);
    }

    public ResResult(boolean success, String code, String message) {
        this(success, code, message, (T) null, (Throwable) null);
    }

    public ResResult(boolean success, String code, String message, T data) {
        this(success, code, message, data, (Throwable) null);
    }

    public ResResult(boolean success, String code, String message, T data, Throwable cause) {
        this.success = true;
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T extends Serializable> ResResult<T> success(String code, String message, T data) {
        ResResult rest = new ResResult();
        rest.setCode(code);
        rest.setMessage(message);
        rest.setData(data);
        rest.setSuccess(true);
        return rest;
    }

    public static <T extends Serializable> ResResult<T> success(T data) {
        ResResult rest = new ResResult();
        rest.setCode("0");
        rest.setMessage("");
        rest.setData(data);
        rest.setSuccess(true);
        return rest;
    }

    public static <T extends Serializable> ResResult<T> fail(String code, String message) {
        ResResult<T> rest = new ResResult();
        rest.setCode(code);
        rest.setMessage(message);
        rest.setSuccess(false);
        return rest;
    }

}