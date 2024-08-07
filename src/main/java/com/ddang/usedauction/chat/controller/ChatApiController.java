package com.ddang.usedauction.chat.controller;

import com.ddang.usedauction.chat.domain.dto.ChatMessageSendDto;
import com.ddang.usedauction.chat.domain.dto.ChatRoomCreateDto;
import com.ddang.usedauction.chat.service.ChatMessageService;
import com.ddang.usedauction.chat.service.ChatRoomService;
import com.ddang.usedauction.config.GlobalApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatApiController {

  private final SimpMessagingTemplate simpMessagingTemplate;
  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;

  @MessageMapping("/chat/message")
  public ResponseEntity<GlobalApiResponse<?>> send(
      @RequestBody ChatMessageSendDto.Request message) {
    simpMessagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    return ResponseEntity.status(HttpStatus.OK)
        .body(GlobalApiResponse.toGlobalResponse(HttpStatus.OK,
            chatMessageService.sendMessage(message)));
  }

  // TODO JWT 정보로 회원정보 받기
  @GetMapping("/api/rooms/{memberId}")
  public ResponseEntity<GlobalApiResponse<?>> getChatRoomList(
      @PathVariable(name = "memberId") Long memberId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(GlobalApiResponse.toGlobalResponse(HttpStatus.OK,
            chatRoomService.getChatRoomList(memberId)));
  }

  /**
   * 임시로 만든 API - 채팅방 생성
   */
  @PostMapping("api/rooms/post/{memberId}")
  public ResponseEntity<GlobalApiResponse<ChatRoomCreateDto.Response>> createChatRoom(
      @PathVariable(name = "memberId") Long memberId,
      @RequestBody ChatRoomCreateDto.Request request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(GlobalApiResponse.toGlobalResponse(HttpStatus.OK,
            chatRoomService.createChatRoom(memberId, request)));

  }
}
