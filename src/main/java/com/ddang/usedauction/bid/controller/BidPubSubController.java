package com.ddang.usedauction.bid.controller;

import com.ddang.usedauction.bid.dto.BidMessageDto;
import com.ddang.usedauction.bid.service.BidPubSubService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class BidPubSubController {

    private final BidPubSubService bidPubSubService;

    /**
     * 입찰하기 컨트롤러
     *
     * @param message 입찰 정보
     */
    @MessageMapping("/bid")
    public void handleBid(BidMessageDto.Request message) {

        bidPubSubService.createBid(message);
    }
}
