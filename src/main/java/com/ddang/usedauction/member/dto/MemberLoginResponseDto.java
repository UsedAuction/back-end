package com.ddang.usedauction.member.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class MemberLoginResponseDto {

    String accessToken;
    String memberId;

    public static MemberLoginResponseDto from(String accessToken, String memberId) {
        return MemberLoginResponseDto.builder()
            .accessToken(accessToken)
            .memberId(memberId)
            .build();
    }
}
