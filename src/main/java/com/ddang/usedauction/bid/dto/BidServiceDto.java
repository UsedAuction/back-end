package com.ddang.usedauction.bid.dto;

import com.ddang.usedauction.auction.dto.AuctionServiceDto;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// todo : 회원 dto로 추가
// 서비스에서 사용할 dto
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class BidServiceDto implements Serializable {

    private Long id;
    private long bidPrice; // 입찰가
    private AuctionServiceDto auction; // 입찰한 경매
    private LocalDateTime createdAt; // 생성 날짜
}
