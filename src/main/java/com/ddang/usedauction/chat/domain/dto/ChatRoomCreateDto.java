package com.ddang.usedauction.chat.domain.dto;

import com.ddang.usedauction.auction.dto.AuctionGetDto;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.member.dto.MemberGetDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatRoomCreateDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class Response implements Serializable {

        private Long id;
        private MemberGetDto.Response seller;
        private MemberGetDto.Response buyer;
        private AuctionGetDto.Response auction;

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime createdAt;

        public static Response from(ChatRoom chatRoom) {
            return Response.builder()
                .id(chatRoom.getId())
                .seller(MemberGetDto.Response.from(chatRoom.getSeller()))
                .buyer(MemberGetDto.Response.from(chatRoom.getBuyer()))
                .auction(AuctionGetDto.Response.from(chatRoom.getAuction()))
                .createdAt(chatRoom.getCreatedAt())
                .build();
        }
    }
}
