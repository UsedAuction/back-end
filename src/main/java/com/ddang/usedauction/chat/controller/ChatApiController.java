package com.ddang.usedauction.chat.controller;

import com.ddang.usedauction.chat.domain.dto.ChatMessageSendDto;
import com.ddang.usedauction.chat.domain.dto.ChatRoomCreateDto;
import com.ddang.usedauction.chat.service.ChatMessageService;
import com.ddang.usedauction.chat.service.ChatRoomService;
import com.ddang.usedauction.chat.service.RedisPublisher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatApiController {

  private final ChatRoomService chatRoomService;
  private final RedisPublisher redisPublisher;
  private final ChatMessageService chatMessageService;

  @MessageMapping("/chat/message")
  public void send(
      @RequestBody ChatMessageSendDto.Request message) {
    ChatMessageSendDto.Response.from(chatMessageService.sendMessage(message));
    redisPublisher.publish(chatRoomService.getTopic(message.getRoomId()), message);

  }

  // TODO JWT 정보로 회원정보 받기
  @GetMapping("/api/rooms/{memberId}")
  public ResponseEntity<List<ChatRoomCreateDto.Response>> getChatRoomList(
      @PathVariable("memberId") Long memberId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(chatRoomService.findChatRoomsByMemberId(memberId));
  }

}
