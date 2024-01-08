package com.cbz.universityforumsystem.exception;

import static com.cbz.universityforumsystem.constant.MessageConstant.UNAUTHORIZED;

public class UnAuthorizedException extends BaseException{
    public UnAuthorizedException() {
        super(UNAUTHORIZED);
    }

    public UnAuthorizedException(String message) {
        super(message);
    }
}
