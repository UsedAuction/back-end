package com.ddang.usedauction.Member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {

  NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 회원입니다.")

  ;

  private final int status;
  private final String message;
}
