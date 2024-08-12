package com.ddang.usedauction.config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisRepoConfig {

  @Value("${spring.sql.data.redis.host}")
  private String host;
  @Value("${spring.sql.data.redis.port}")
  private int port;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(host, port);
  }
}
