package com.cbz.universityforumsystem.exception;

public class SystemErrorException extends BaseException{
    public SystemErrorException() {
        super("系统出错了");
    }

    public SystemErrorException(String message) {
        super(message);
    }
}
