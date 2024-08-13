package com.ddang.usedauction.config;

import com.ddang.usedauction.payment.exception.PaymentReadyException;
import com.ddang.usedauction.payment.exception.PaymentApproveException;
import com.ddang.usedauction.auction.exception.MemberPointOutOfBoundsException;
import com.ddang.usedauction.image.exception.ImageDeleteFailException;
import com.ddang.usedauction.image.exception.ImageUploadFailException;
import com.ddang.usedauction.payment.exception.PaymentRequestTimeoutException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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

    // 404 에러 핸들러
    @ExceptionHandler(NoHandlerFoundException.class)
    private ResponseEntity<String> handleNotFoundException(
        NoHandlerFoundException e) {

        log.error("404 NotFound", e);

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body("존재하지 않는 endPoint입니다.");
    }

    // 405 에러 핸들러
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    private ResponseEntity<String> handleNotSupportedException(
        HttpRequestMethodNotSupportedException e) {

        log.error("405 NotSupported", e);

        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body("해당 url을 지원하지 않습니다. HTTP Method(GET, PUT, POST, DELETE)가 정확한지 확인해주세요.");
    }

    // 유효성 검증 에러 핸들러(requestBody) -> 400 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<List<String>> handleValidException(
        MethodArgumentNotValidException e) {

        log.error("request 유효성 검사 실패", e);

        List<String> list = new ArrayList<>();
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            list.add(fieldError.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(list);
    }

    // 유효성 검증 에러 핸들러(pathVariable, requestParam) -> 400 에러
    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<List<String>> handleValidException2(
        ConstraintViolationException e) {

        log.error("pathVariable 또는 requestParam 유효성 검사 실패", e);

        List<String> list = new ArrayList<>();
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            list.add(constraintViolation.getMessage());
        }

        return ResponseEntity.badRequest().body(list);
    }

    // 필수 PathVariable 값 존재하지 않을 경우 에러 핸들러
    @ExceptionHandler(MissingPathVariableException.class)
    private ResponseEntity<String> handleMissingPathVariableException(
        MissingPathVariableException e) {

        log.error("필수 PathVariable 값 존재하지 않음", e);

        return ResponseEntity.badRequest().body(
            "필수 값인 pathVariable 값이 존재하지 않습니다.");
    }

    // 필수 RequestPart 값 존재하지 않을 경우 에러 핸들러
    @ExceptionHandler(MissingServletRequestPartException.class)
    private ResponseEntity<String> handleMissingServletRequestPartException(
        MissingServletRequestPartException e) {

        log.error("필수 RequestPart 값 존재하지 않음", e);

        return ResponseEntity.badRequest()
            .body("필수값인 RequestPart 값이 존재하지 않습니다.");
    }

    // 필수 RequestParam 값 존재하지 않을 경우 에러 핸들러
    @ExceptionHandler(MissingServletRequestParameterException.class)
    private ResponseEntity<String> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException e) {

        log.error("필수 RequestParam 값 존재하지 않은", e);

        return ResponseEntity.badRequest()
            .body("필수값인 RequestParam 값이 존재하지 않습니다.");
    }

    // unique 제약 조건 위반 exception 핸들러
    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<String> handleDataIntegrityViolationException(
        DataIntegrityViolationException e) {

        log.error("unique 제약 조건 위반", e);

        return ResponseEntity.badRequest()
            .body("unique 제약 조건에 위반된 요청입니다. 생성 또는 변경하려는 요청 중 중복된 값이 포함되어있습니다.");
    }

    @ExceptionHandler(MemberPointOutOfBoundsException.class)
    private ResponseEntity<String> handleMemberPointOutOfBoundsException(
        MemberPointOutOfBoundsException e) {

        log.error("회원 구매 확정 시 포인트 부족", e);

        return ResponseEntity.badRequest()
            .body(e.getMessage());
    }

    @ExceptionHandler(ImageDeleteFailException.class)
    private ResponseEntity<String> handleImageDeleteFailException(ImageDeleteFailException e) {

        log.error("s3에서 이미지 삭제 실패", e);

        return ResponseEntity.badRequest()
            .body(e.getMessage());
    }

    @ExceptionHandler(ImageUploadFailException.class)
    private ResponseEntity<String> handleImageUploadFailException(ImageUploadFailException e) {

        log.error("s3에 이미지 업로드 실패", e);

        return ResponseEntity.badRequest()
            .body(e.getMessage());
    }
  
    @ExceptionHandler(PaymentReadyException.class)
    private ResponseEntity<String> handlePaymentReadyException(PaymentReadyException e) {
      
        log.error("PaymentReadyException", e);
      
        return ResponseEntity
            .status(HttpStatus.REQUEST_TIMEOUT)
            .body(e.getMessage());
    }

    @ExceptionHandler(PaymentApproveException.class)
    private ResponseEntity<String> handlePaymentApproveException(PaymentApproveException e) {
      
        log.error("PaymentApproveException", e);
      
        return ResponseEntity
            .status(HttpStatus.REQUEST_TIMEOUT)
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
