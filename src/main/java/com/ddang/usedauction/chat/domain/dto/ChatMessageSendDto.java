package com.ddang.usedauction.chat.domain.dto;

import com.ddang.usedauction.chat.domain.entity.ChatMessage;
import com.ddang.usedauction.member.domain.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatMessageSendDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class Request {

        private Long roomId;
        private String senderId;
        private String message;

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class Response {

        private Long roomId;
        private Member sender;
        private String message;

        public static Response from(ChatMessage chatMessage) {
            return Response.builder()
                .roomId(chatMessage.getChatRoom().getId())
                .sender(chatMessage.getSender())
                .message(chatMessage.getMessage())
                .build();
        }
    }


}
