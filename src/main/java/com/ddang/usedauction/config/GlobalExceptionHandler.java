package com.ddang.usedauction.config;

import com.ddang.usedauction.auction.exception.AuctionException;
import com.ddang.usedauction.category.exception.CategoryException;
import com.ddang.usedauction.image.exception.ImageException;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.payment.exception.PaymentException;
import com.ddang.usedauction.point.exception.PointException;
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
    private ResponseEntity<GlobalApiResponse<?>> handleNotFoundException(
        NoHandlerFoundException e) {

        log.error("404 NotFound", e);

        return new ResponseEntity<>(
            GlobalApiResponse.toGlobalResponseFail(HttpStatus.NOT_FOUND, "요청한 페이지를 찾을 수 없습니다."),
            HttpStatus.NOT_FOUND);
    }

    // 405 에러 핸들러
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleNotSupportedException(
        HttpRequestMethodNotSupportedException e) {

        log.error("405 NotSupported", e);

        return new ResponseEntity<>(
            GlobalApiResponse.toGlobalResponseFail(HttpStatus.METHOD_NOT_ALLOWED,
                "해당 url을 지원하지 않습니다. HTTP Method(GET, PUT, POST, DELETE)가 정확한지 확인해주세요."),
            HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 유효성 검증 에러 핸들러(requestBody) -> 400 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<List<GlobalApiResponse<?>>> handleValidException(
        MethodArgumentNotValidException e) {

        log.error("request 유효성 검사 실패", e);

        List<GlobalApiResponse<?>> list = new ArrayList<>();
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            GlobalApiResponse<?> response = GlobalApiResponse.toGlobalResponseFail(
                HttpStatus.BAD_REQUEST,
                fieldError.getDefaultMessage());
            list.add(response);
        }

        return ResponseEntity.badRequest().body(list);
    }

    // 유효성 검증 에러 핸들러(pathVariable, requestParam) -> 400 에러
    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<List<GlobalApiResponse<?>>> handleValidException2(
        ConstraintViolationException e) {

        log.error("pathVariable 또는 requestParam 유효성 검사 실패", e);

        List<GlobalApiResponse<?>> list = new ArrayList<>();
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            GlobalApiResponse<Object> response = GlobalApiResponse.toGlobalResponseFail(
                HttpStatus.BAD_REQUEST,
                constraintViolation.getMessage());
            list.add(response);
        }

        return ResponseEntity.badRequest().body(list);
    }

    // 필수 PathVariable 값 존재하지 않을 경우 에러 핸들러
    @ExceptionHandler(MissingPathVariableException.class)
    private ResponseEntity<GlobalApiResponse<String>> handleMissingPathVariableException(
        MissingPathVariableException e) {

        log.error("필수 PathVariable 값 존재하지 않음", e);

        return ResponseEntity.badRequest().body(
            GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                "PathVariable 값은 필수값입니다."));
    }

    // 필수 RequestPart 값 존재하지 않을 경우 에러 핸들러
    @ExceptionHandler(MissingServletRequestPartException.class)
    private ResponseEntity<GlobalApiResponse<String>> handleMissingServletRequestPartException(
        MissingServletRequestPartException e) {

        log.error("필수 RequestPart 값 존재하지 않음", e);

        return ResponseEntity.badRequest()
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                "필수값인 RequestPart 값이 존재하지 않습니다."));
    }

    // 필수 RequestParam 값 존재하지 않을 경우 에러 핸들러
    @ExceptionHandler(MissingServletRequestParameterException.class)
    private ResponseEntity<GlobalApiResponse<String>> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException e) {

        log.error("필수 RequestParam 값 존재하지 않은", e);

        return ResponseEntity.badRequest()
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                "필수값인 RequestParam 값이 존재하지 않습니다."));
    }

    // unique 제약 조건 위반 exception 핸들러
    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<GlobalApiResponse<String>> handleDataIntegrityViolationException(
        DataIntegrityViolationException e) {

        log.error("unique 제약 조건 위반", e);

        return ResponseEntity.badRequest()
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                "unique 제약 조건에 위반된 요청입니다. 생성 또는 변경하려는 요청 중 중복된 값이 포함되어있습니다."));
    }

    // 회원 관련 에러 핸들러 -> 400 에러
    @ExceptionHandler(MemberException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleMemberException(MemberException e) {

        log.error("회원 관련 exception", e);

        return ResponseEntity.badRequest()
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                e.getMemberErrorCode().getMessage()));
    }

    // 경매 관련 에러 핸들러 -> 400 에러
    @ExceptionHandler(AuctionException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleAuctionException(AuctionException e) {

        log.error("경매 관련 exception", e);

        return ResponseEntity.badRequest()
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                e.getAuctionErrorCode().getMessage()));
    }

    // 카테고리 관련 에러 핸들러 -> 400 에러
    @ExceptionHandler(CategoryException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleCategoryException(CategoryException e) {

        log.error("카테고리 관련 exception", e);

        return ResponseEntity.badRequest()
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                e.getCategoryErrorCode().getMessage()));
    }

    // 이미지 관련 에러 핸들러 -> 400 에러
    @ExceptionHandler(ImageException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleImageException(ImageException e) {

        log.error("이미지 관련 exception", e);

        return ResponseEntity.badRequest()
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                e.getImageErrorCode().getMessage()));
    }

    // 포인트 관련 에러 핸들러 -> 400 에러
    @ExceptionHandler(PointException.class)
    public ResponseEntity<GlobalApiResponse<?>> pointExceptionHandler(PointException e) {

        log.error("PointException", e);

        return ResponseEntity.badRequest()
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                e.getPointErrorCode().getMessage()));
    }

    // 결제 관련 에러 핸들러 -> 400 에러
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<?> paymentExceptionHandler(PaymentException e) {
        log.error("PaymentException", e);

        return ResponseEntity.badRequest()
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
                e.getPaymentErrorCode().getMessage()));
    }

//    // 예상하지 못한 에러 핸들러 -> 500 에러
//    @ExceptionHandler(Exception.class)
//    private ResponseEntity<GlobalApiResponse<?>> handleUnexpectedException(Exception e) {
//
//        log.error("예상하지 못한 에러", e);
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.INTERNAL_SERVER_ERROR,
//                "예상치 못한 에러가 발생했습니다. 서버 관리자에게 문의하세요. message = " + e.getMessage()));
//    }
}
