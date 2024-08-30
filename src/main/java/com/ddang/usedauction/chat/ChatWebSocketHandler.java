package com.ddang.usedauction.chat;

import com.ddang.usedauction.security.jwt.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final SetOperations<String, String> setOperations;
    private final TokenProvider tokenProvider;

    public ChatWebSocketHandler(
        @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate,
        TokenProvider tokenProvider) {

        this.setOperations = redisTemplate.opsForSet();
        this.tokenProvider = tokenProvider;
    }

    private static final String CHAT_ROOM_PREFIX = "CHAT_ROOM_ID";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String header = session.getHandshakeHeaders().getFirst("Authorization");

        String token = tokenProvider.resolveTokenFromHeader(header);
        String memberId = tokenProvider.getMemberIdByToken(token);

        String chatRoomId = (String) session.getAttributes().get("chatRoomId"); // 채팅방 ID를 세션에서 가져오기

        // Redis에 사용자가 채팅방에 들어왔음을 저장
        setOperations.add(CHAT_ROOM_PREFIX + chatRoomId, memberId);
        log.info("memebrId : {}", memberId + " " + "entered chat room " + chatRoomId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
        throws Exception {
        // WebSocket 세션의 핸드셰이크 헤더에서 Authorization 헤더 가져오기
        String header = session.getHandshakeHeaders().getFirst("Authorization");

        String token = tokenProvider.resolveTokenFromHeader(header);
        String memberId = tokenProvider.getMemberIdByToken(token);

        String chatRoomId = (String) session.getAttributes().get("chatRoomId");

        // Redis에서 사용자의 채팅방 입장 상태 제거
        setOperations.remove(CHAT_ROOM_PREFIX + chatRoomId, memberId);
        log.info("memebrId : {}", memberId + " " + "left chat room " + chatRoomId);
    }
}
