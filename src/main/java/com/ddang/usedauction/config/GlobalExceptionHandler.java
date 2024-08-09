package com.ddang.usedauction.config;


import com.ddang.usedauction.chat.exception.ChatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ChatException.class)
  private ResponseEntity<GlobalApiResponse<?>> handleChatException(ChatException e) {
    log.error("채팅 관련 exception", e);

    return ResponseEntity.badRequest()
        .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.BAD_REQUEST,
            e.getChatErrorCode().getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<GlobalApiResponse<?>> exceptionHandler(Exception e) {
    log.error("Exception", e);

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error. message = " + e.getMessage()));
  }
}
