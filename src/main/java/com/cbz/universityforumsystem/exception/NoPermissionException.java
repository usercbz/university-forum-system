package com.cbz.universityforumsystem.exception;

public class NoPermissionException extends BaseException {
    public NoPermissionException() {
        super("权限不足，没有相应的权限!");
    }

    public NoPermissionException(String message) {
        super(message);
    }
}
