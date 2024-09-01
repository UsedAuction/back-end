package com.ddang.usedauction.member.dto;

import com.ddang.usedauction.member.domain.MemberHistory;
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
public class MemberHistoryDto {

    private String memberId;
    private String loginStatus;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
    private LocalDateTime loginTime;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
    private LocalDateTime logoutTime;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
    private LocalDateTime refreshTokenExpiry;

    public static MemberHistoryDto from(MemberHistory memberHistory) {
        return MemberHistoryDto.builder()
            .memberId(memberHistory.getMember().getMemberId())
            .loginStatus(memberHistory.getLoginStatus().toString())
            .loginTime(memberHistory.getLoginTime())
            .logoutTime(memberHistory.getLogoutTime())
            .refreshTokenExpiry(memberHistory.getRefreshTokenExpiry())
            .build();

    }
}
