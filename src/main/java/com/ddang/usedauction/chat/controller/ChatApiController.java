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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * 회원별 채팅방 목록 조회
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<List<ChatRoomCreateDto.Response>> getChatRoomList(
        @AuthenticationPrincipal PrincipalDetails principalDetails) {

        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.findChatRoomsByMemberId(principalDetails.getName()));
    }

    /**
     * 채팅방 메시지 조회
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/api/chat/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageSendDto.Response>> getChatMessages(
        @AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long roomId) {

        return ResponseEntity.status(HttpStatus.OK)
            .body(chatMessageService.findMessagesByChatRoomId(principalDetails.getName(), roomId));
    }

    /**
     * 경매 제목으로 채팅방 검색
     */
    @GetMapping("/api/chat/rooms/search")
    public ResponseEntity<List<ChatRoomCreateDto.Response>> searchChatRooms(
        @RequestParam("title") String title) {

        return ResponseEntity.status(HttpStatus.OK)
            .body(chatRoomService.searchChatRoomByAuctionTitle(title));
    }

    /**
     * 채팅방 입장한 회원 redis 저장
     */
    @PostMapping("/api/chat/room/{roomId}/enter")
    public ResponseEntity<Void> enterChatRoom(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @PathVariable long roomId) {

        chatRoomService.enterChatRoom(roomId, principalDetails.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 나간 회원 redis 삭제
     */
    @PostMapping("/api/chat/room/{roomId}/exit")
    public ResponseEntity<Void> exitChatRoom(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @PathVariable long roomId) {

        chatRoomService.exitChatRoom(roomId, principalDetails.getName());
        return ResponseEntity.ok().build();
    }

}
