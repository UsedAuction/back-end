package com.ddang.usedauction.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
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

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
    private LocalDateTime createdAt;

    public static MemberLoginResponseDto from(String accessToken, String memberId) {
        return MemberLoginResponseDto.builder()
            .accessToken(accessToken)
            .memberId(memberId)
            .build();
    }
}
