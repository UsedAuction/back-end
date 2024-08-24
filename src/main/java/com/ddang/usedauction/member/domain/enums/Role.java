package com.ddang.usedauction.member.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
  ROLE_USER("일반 회원");
  private String value;
}
