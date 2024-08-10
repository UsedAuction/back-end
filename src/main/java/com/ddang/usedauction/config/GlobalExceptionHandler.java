package com.ddang.usedauction.config;

import com.ddang.usedauction.payment.exception.PaymentRequestTimeoutException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 결제 승인 요청을 보냈지만 응답을 받지 못했을 때 에러 핸들러
    @ExceptionHandler(PaymentRequestTimeoutException.class)
    private ResponseEntity<String> handlePaymentException(PaymentRequestTimeoutException e) {
        log.error("PaymentException", e);
        return ResponseEntity
            .status(HttpStatus.REQUEST_TIMEOUT)
            .body(e.getMessage());
    }

    // 404 에러 핸들러
    @ExceptionHandler(NoHandlerFoundException.class)
    private ResponseEntity<String> handleNotFoundException(NoHandlerFoundException e) {
        log.error("NoHandlerFoundException", e);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(e.getMessage());
    }

    // 405 에러 핸들러
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    private ResponseEntity<String> handleNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException", e);
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(e.getMessage());
    }

    // 유효성 검증 에러 핸들러(requestBody) -> 400 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<String> handleValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
    }

    // 유효성 검증 에러 핸들러(pathVariable, requestParam) -> 400 에러
    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<String> handleValidException2(ConstraintViolationException e) {
        log.error("ConstraintViolationException", e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
    }

    // 필수 PathVariable 값 존재하지 않을 경우 에러 핸들러
    @ExceptionHandler(MissingPathVariableException.class)
    private ResponseEntity<String> handleMissingPathVariableException(MissingPathVariableException e) {
        log.error("MissingPathVariableException", e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
    }

    // 필수 RequestPart 값 존재하지 않을 경우 에러 핸들러
    @ExceptionHandler(MissingServletRequestPartException.class)
    private ResponseEntity<String> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        log.error("MissingServletRequestPartException", e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
    }

    // 필수 RequestParam 값 존재하지 않을 경우 에러 핸들러
    @ExceptionHandler(MissingServletRequestParameterException.class)
    private ResponseEntity<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException", e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
    }

    // unique 제약 조건 위반 exception 핸들러
    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException", e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
    }

    // 엔티티가 존재하지 않을때
    @ExceptionHandler(EntityNotFoundException.class)
    private ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("EntityNotFoundException", e);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    private ResponseEntity<String> handleNoSuchElementException(NoSuchElementException e) {
        log.error("NoSuchElementException", e);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    private ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<?> handleException(Exception e) {
        log.error("Exception", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(e.getMessage());
    }
}
