package com.ddang.usedauction.chat.domain.dto;

import com.ddang.usedauction.auction.dto.AuctionGetForChatDto;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.member.dto.MemberGetForChatDto;
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
        private MemberGetForChatDto seller;
        private MemberGetForChatDto buyer;
        private AuctionGetForChatDto auction;
        private int unReadCnt;
        private String lastMessage;

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime lastMessageTime;

        public static Response from(ChatRoom chatRoom) {
            return Response.builder()
                .id(chatRoom.getId())
                .seller(MemberGetForChatDto.from(chatRoom.getSeller()))
                .buyer(MemberGetForChatDto.from(chatRoom.getBuyer()))
                .auction(AuctionGetForChatDto.from(chatRoom.getAuction()))
                .build();
        }
    }
}
