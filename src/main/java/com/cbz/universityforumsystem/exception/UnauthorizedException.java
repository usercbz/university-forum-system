package com.cbz.universityforumsystem.exception;

import static com.cbz.universityforumsystem.constant.MessageConstant.UNAUTHORIZED;

public class UnauthorizedException extends  BaseException{
    public UnauthorizedException() {
        super(UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
