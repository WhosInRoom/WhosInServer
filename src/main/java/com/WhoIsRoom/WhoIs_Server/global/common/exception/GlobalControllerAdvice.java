package com.WhoIsRoom.WhoIs_Server.global.common.exception;

import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;

import static com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode.*;


@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    // 요청한 api가 없을 경우
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public BaseErrorResponse handle_NoHandlerFoundException(NoHandlerFoundException e){
        log.error("[handle_NoHandlerFoundException]", e);
        return new BaseErrorResponse(NOT_FOUND);
    }

    // 잘못된 인자를 넘긴 경우 & DTO 검증에 실패한 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public BaseErrorResponse handle_IllegalArgumentException(Exception e) {
        log.error("[handle_BadRequest]", e);

        if(e instanceof MethodArgumentNotValidException) {
            return new BaseErrorResponse(ILLEGAL_ARGUMENT, (Objects.requireNonNull(((MethodArgumentNotValidException) e).getBindingResult().getFieldError()).getDefaultMessage()));
        }

        return new BaseErrorResponse(ILLEGAL_ARGUMENT);
    }

    // Http 메서드가 유효하지 않은 경우
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseErrorResponse handle_HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("[handle_HttpRequestMethodNotSupportedException]", e);
        return new BaseErrorResponse(METHOD_NOT_ALLOWED);
    }

    // 런타임 오류가 발생한 경우
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public BaseErrorResponse handle_RuntimeException(RuntimeException e) {
        log.error("[handle_RuntimeException]", e);
        return new BaseErrorResponse(SERVER_ERROR);
    }

    // 커스텀 에러가 발생한 경우
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseErrorResponse> handleCustomExceptions(BusinessException e) {
        log.error("[handle_BusinessException]", e);
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(new BaseErrorResponse(e.getErrorCode()));
    }

    // 예상치 못한 모든 예외가 발생한 경우
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public BaseErrorResponse handle_Exception(Exception e) {
        log.error("[handle_Exception]", e);
        return new BaseErrorResponse(SERVER_ERROR);
    }
}

