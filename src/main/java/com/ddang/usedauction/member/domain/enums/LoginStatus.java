package com.ddang.usedauction.member.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginStatus {
    LOGIN("로그인 상태"), LOGOUT("로그아웃 상태");
    private String value;
}
