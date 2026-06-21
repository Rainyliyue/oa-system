package com.oa.common.exception;

import com.oa.common.result.AjaxResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public AjaxResult<Void> duplicateKey(DuplicateKeyException exception) {
        return AjaxResult.error("数据已存在，请检查唯一字段是否重复");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult<Void> argumentInvalid(MethodArgumentNotValidException exception) {
        if (exception.getBindingResult().hasFieldErrors()) {
            return AjaxResult.error(exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        }
        return AjaxResult.error("请求参数不正确");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AjaxResult<Void> messageNotReadable(HttpMessageNotReadableException exception) {
        return AjaxResult.error("请求参数格式不正确");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult<Void> methodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return AjaxResult.error("请求方法不支持");
    }

    @ExceptionHandler(Exception.class)
    public AjaxResult<Void> handle(Exception exception) {
        return AjaxResult.error(exception.getMessage() == null ? "系统异常，请稍后重试" : exception.getMessage());
    }
}
