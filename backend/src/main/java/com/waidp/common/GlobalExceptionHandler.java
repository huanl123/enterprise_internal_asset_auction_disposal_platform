package com.waidp.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<Void> handle(Exception ex) {
        return Result.error(500, resolveMessage(ex));
    }

    private String resolveMessage(Exception ex) {
        if (ex == null) {
            return "请求处理失败";
        }
        if (ex instanceof IllegalArgumentException) {
            return ex.getMessage();
        }
        if (ex instanceof MethodArgumentNotValidException manv
                && manv.getBindingResult().getFieldError() != null) {
            return manv.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null) {
            return be.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (ex instanceof ConstraintViolationException cve && !cve.getConstraintViolations().isEmpty()) {
            return cve.getConstraintViolations().iterator().next().getMessage();
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "请求参数格式不正确";
        }
        String message = ex.getMessage();
        return (message == null || message.isBlank()) ? "请求参数不合法" : message;
    }
}

