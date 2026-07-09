package com.example.chat.common.config;

import com.example.chat.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntime(RuntimeException e) {
        return Result.error(400, e.getMessage(), null);
    }
}
