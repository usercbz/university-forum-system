package com.cbz.universityforumsystem.exception;


import static com.cbz.universityforumsystem.constant.MessageConstant.SEND_MAIL_FAIL;

public class SendMailFailException extends BaseException {


    public SendMailFailException() {
        super(SEND_MAIL_FAIL);
    }

    public SendMailFailException(String message) {
        super(message);
    }
}
