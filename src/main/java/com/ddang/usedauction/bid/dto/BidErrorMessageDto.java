package com.ddang.usedauction.bid.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class BidErrorMessageDto { // 에러 메시지 dto

    private BidStatus bidStatus; // 입찰 성공 유무
    private String message; // 에러 메시지

    // BidErrorMessageDto로 변환하는 메소드
    public static BidErrorMessageDto from(String message) {

        return BidErrorMessageDto.builder()
            .bidStatus(BidStatus.ERROR)
            .message(message)
            .build();
    }
}
