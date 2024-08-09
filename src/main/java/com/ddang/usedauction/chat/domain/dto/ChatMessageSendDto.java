package com.ddang.usedauction.chat.domain.dto;

import com.ddang.usedauction.Member.Member;
import com.ddang.usedauction.chat.domain.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

public class ChatMessageSendDto {

  @Getter
  public static class Request {

    private Long roomId;
    private Long senderId;
    private String message;

  }

  @Getter
  @Builder
  public static class Response {

    private Long roomId;
    private Member sender;
    private String message;

    public static Response of(ChatMessage chatMessage) {
      return Response.builder()
          .roomId(chatMessage.getChatRoom().getId())
          .sender(chatMessage.getSender())
          .message(chatMessage.getMessage())
          .build();
    }
  }


}
