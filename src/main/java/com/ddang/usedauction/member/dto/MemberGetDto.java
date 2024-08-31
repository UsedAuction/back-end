package com.ddang.usedauction.member.dto;

import com.ddang.usedauction.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberGetDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response implements Serializable {

        private Long id;
        private String memberId;
        private String email;
        private boolean siteAlarm;
        private long point;
        private String social;
        private String socialProviderId;

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime createdAt;

        // entity -> getResponse
        public static MemberGetDto.Response from(Member member) {

            return Response.builder()
                .id(member.getId())
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .siteAlarm(member.isSiteAlarm())
                .point(member.getPoint())
                .social(member.getSocial())
                .socialProviderId(member.getSocialProviderId())
                .createdAt(member.getCreatedAt())
                .build();
        }
    }
}
