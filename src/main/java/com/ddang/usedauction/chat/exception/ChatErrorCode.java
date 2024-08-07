package com.ddang.usedauction.chat.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatErrorCode {

  NOT_FOUND_CHAT_ROOM(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 채팅방입니다.."),
  ALREADY_EXISTS_CHATROOM(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 채팅방입니다.");

  private final int status;
  private final String message;
}
