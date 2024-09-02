package com.ddang.usedauction.chat;

import com.ddang.usedauction.security.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;

    public ChatWebSocketHandler(
        @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate,
        TokenProvider tokenProvider) {
        this.redisTemplate = redisTemplate;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = tokenProvider.resolveTokenFromHeader(
            session.getHandshakeHeaders().getFirst("Authorization"));

        String memberId = tokenProvider.getMemberIdByToken(token);

        String roomId = (String) session.getAttributes().get("roomId");

        redisTemplate.opsForSet().add("CHAT_ROOM:" + roomId + ":MEMBERS", memberId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
        throws Exception {
        String token = tokenProvider.resolveTokenFromHeader(
            session.getHandshakeHeaders().getFirst("Authorization"));

        String memberId = tokenProvider.getMemberIdByToken(token);
        String roomId = (String) session.getAttributes().get("roomId");

        redisTemplate.opsForSet().remove("CHAT_ROOM:" + roomId + ":MEMBERS", memberId);

    }
}
