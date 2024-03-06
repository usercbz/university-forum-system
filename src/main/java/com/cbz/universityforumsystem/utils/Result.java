package com.cbz.universityforumsystem.utils;

import lombok.Data;

/**
 * 结果处理
 */
@Data
public class Result {
    private int code;
    private Object data;
    private String message;

    public Result(int code, Object data) {
        this.code = code;
        this.data = data;
    }

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(int code) {
        this.code = code;
    }

    public static Result success(Object data) {
        return new Result(200, data);
    }

    public static Result success() {
        return new Result(200);
    }

    public static Result error(String msg) {
        return new Result(500, msg);
    }
}
