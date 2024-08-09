package com.ddang.usedauction.chat.service;

import com.ddang.usedauction.chat.domain.dto.ChatMessageSendDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * 채팅방에 입장해 메시지를 작성하면 해당 메시지를 Redis Topic에 발행하는 기능
   */
  public void publish(ChannelTopic topic, ChatMessageSendDto.Request message) {
    redisTemplate.convertAndSend(topic.getTopic(), message);
  }
}
