package com.ddang.usedauction.chat.domain.dto;

import com.ddang.usedauction.Member.Member;
import com.ddang.usedauction.chat.domain.entity.ChatMessage;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

public class ChatMessageSendDto {

  @Getter
  public static class Request {

    private Long roomId;
    private Long senderId;
    private String message;

    public ChatMessage toMessage() {
      return ChatMessage.builder()
          .chatRoom(ChatRoom.builder().id(roomId).build())
          .sender(Member.builder().id(senderId).build())
          .message(message)
          .build();
    }

    public Response toResponse(Member member) {
      return Response.builder()
          .roomId(roomId)
          .sender(member)
          .message(message)
          .build();
    }
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
