package com.cbz.universityforumsystem.exception;

public class UploadFileException extends BaseException{

    public UploadFileException() {
        super("文件上传失败");
    }

    public UploadFileException(String message) {
        super(message);
    }
}
