package com.ddang.usedauction.bid.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 서비스에서 사용할 dto
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class BidServiceDto implements Serializable {

    private Long id;
    private long bidPrice; // 입찰가
    private Long auctionId; // 입찰한 경매 PK
    private String memberId; // 입찰한 회원 id
    private LocalDateTime createdAt; // 생성 날짜
}
