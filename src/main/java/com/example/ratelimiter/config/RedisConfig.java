package com.example.ratelimiter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

  @Value("${redis.hostname:localhost}")
  private String redisHostName;

  @Value("${redis.port:6379}")
  private Integer redisPort;

  @Bean
  RedisStandaloneConfiguration redisStandaloneConfiguration() {
    return new RedisStandaloneConfiguration(redisHostName, redisPort);
  }

  @Bean
  LettuceConnectionFactory jedisConnectionFactory() {
    return new LettuceConnectionFactory(redisStandaloneConfiguration());
  }

  @Bean
  StringRedisTemplate redisTemplate() {
    StringRedisTemplate template = new StringRedisTemplate();
    template.setConnectionFactory(jedisConnectionFactory());
    template.setEnableTransactionSupport(true);

    return template;
  }
}
