package com.ddang.usedauction.config.redis;

import com.ddang.usedauction.chat.service.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  private final RedisRepoConfig redisRepoConfig;

  /**
   * redis에 publish된 메시지 처리를 위한 리스너 설정
   */
  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      RedisConnectionFactory redisConnectionFactory,
      MessageListenerAdapter listenerAdapter,
      ChannelTopic channelTopic
  ) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(listenerAdapter, channelTopic);

    return container;
  }

  /**
   * 실제 메시지 처리하는 subscriber 설정
   */
  @Bean
  public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
    return new MessageListenerAdapter(subscriber, "onMessage");
  }

  /**
   * 단일 Topic 사용을 위한 Bean
   */
  @Bean
  public ChannelTopic channelTopic() {
    return new ChannelTopic("sub/chat/room");
  }

  /**
   * RedisConnectionFactory : Redis 연결을 관리하는 팩토리 Key Serializer : 키를 문자열로 직렬화하기 위해 사용 Value
   * Serializer : 값을 JSON 형식으로 직렬화하기 위해 사용
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

    return redisTemplate;
  }
}
