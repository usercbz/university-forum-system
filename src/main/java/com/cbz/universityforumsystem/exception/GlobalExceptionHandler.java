package com.cbz.universityforumsystem.exception;

import com.cbz.universityforumsystem.utils.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Result exceptionHandler(BaseException ex) {
        return Result.error(ex.getMessage());
    }

}
