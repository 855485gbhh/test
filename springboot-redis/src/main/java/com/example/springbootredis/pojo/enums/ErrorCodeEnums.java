package com.example.springbootredis.pojo.enums;

public enum ErrorCodeEnums {

    DEFAULT(0, "success"),
    SYSTEM_ERROR(49999, "system error"),
    USER_NOT_CHECK_AUTH(40100, "user unauthenticated");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCodeEnums(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

