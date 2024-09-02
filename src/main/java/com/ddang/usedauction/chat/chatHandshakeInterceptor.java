package com.ddang.usedauction.chat;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
public class chatHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        String roomId = extractRoomIdFromRequest(request);

        if (roomId != null) {
            attributes.put("roomId", roomId);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Exception exception) {

    }

    private String extractRoomIdFromRequest(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        log.info("interceptor path : {}", path);

        String[] segments = path.split("/");

        if (segments.length > 0) {
            return segments[segments.length - 1];
        }
        return null;
    }

}
