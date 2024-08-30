package com.ddang.usedauction.chat.controller;

import com.ddang.usedauction.chat.domain.dto.ChatMessageSendDto;
import com.ddang.usedauction.chat.domain.dto.ChatRoomCreateDto;
import com.ddang.usedauction.chat.service.ChatMessageService;
import com.ddang.usedauction.chat.service.ChatRoomService;
import com.ddang.usedauction.chat.service.RedisPublisher;
import com.ddang.usedauction.security.auth.PrincipalDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public void send(@RequestBody ChatMessageSendDto.Request request) {

        chatMessageService.sendMessage(request);
        redisPublisher.publish(chatRoomService.getTopic(request.getRoomId()), request);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<List<ChatRoomCreateDto.Response>> getChatRoomList(
        @AuthenticationPrincipal PrincipalDetails principalDetails) {

        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findChatRoomsByMemberId(principalDetails.getName()));
    }
  
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/api/chat/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageSendDto.Response>> getChatMessages(
        @AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long roomId) {

        return ResponseEntity.status(HttpStatus.OK)
            .body(chatMessageService.findMessagesByChatRoomId(principalDetails.getName(), roomId));
    }

}
