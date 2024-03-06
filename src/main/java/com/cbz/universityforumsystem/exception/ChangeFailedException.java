package com.cbz.universityforumsystem.exception;

public class ChangeFailedException extends BaseException{
    public ChangeFailedException() {
        super("修改失败!");
    }

    public ChangeFailedException(String message) {
        super(message);
    }
}
