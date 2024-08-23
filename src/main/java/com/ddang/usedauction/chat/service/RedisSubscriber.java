package com.ddang.usedauction.chat.service;

import com.ddang.usedauction.chat.domain.dto.ChatMessageSendDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;
  private final SimpMessageSendingOperations sendingOperations;

  /**
   * redis에서 메시지가 발행되면 메시지를 받아 messgingTemplate를 이용해 websocket 클라이언트들에게 메시지 전달
   */
  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String publishMessage = (String) redisTemplate.getStringSerializer()
          .deserialize(message.getBody());
      ChatMessageSendDto.Request dto = objectMapper.readValue(publishMessage,
          ChatMessageSendDto.Request.class);
      sendingOperations.convertAndSend("/sub/chat/room/" + dto.getRoomId(), dto);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}
