package com.ddang.usedauction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /**
   * /sub : 메시지 브로커가 /sub으로 시작하는 주소를 구독한 Subscriber들에게 메시지 전달 /pub : 클라이언트가 서버로 메시지를 발송할 수 있는 경로
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/sub");
    config.setApplicationDestinationPrefixes("/pub");
  }

  /**
   * 소켓 연결을 위한 엔드 포인트 지정 CORS를 피하기 위해 AllowedOriginPatterns "*"으로 지정 테스트를 위해 .withSockJS() 주석처리
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*");
//        .withSockJS();
  }
}
