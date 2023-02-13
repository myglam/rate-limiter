package com.example.ratelimiter.services;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

  public static final Long MAXIMUM_REQUEST_COUNT = 10l;

  @Autowired
  StringRedisTemplate redisTemplate;

  public Boolean isThrottled(String originIP) {
    Long requestCount = getRequestCount(originIP);
    System.out.println("For " + originIP + " we have " + requestCount + " requests.");
    return requestCount >= MAXIMUM_REQUEST_COUNT;
  }

  public Long getExpireInSeconds(String originIP) {
    return redisTemplate.getExpire(originIP);
  }

  public Long getRequestCount(String originIP) {
    String currentValue = redisTemplate.opsForValue().get(originIP);

    if (currentValue == null) {
      return 0l;
    }

    return Long.valueOf(currentValue);
  }

  public void incrementAndThrottle(String originIP) {
    redisTemplate.execute((RedisCallback<String>) connection -> {
      // The WATCH command keeps track of changes on a key
      connection.watch(originIP.getBytes());

      // The MULTI command starts a transaction
      connection.multi();

      // Write operations
      Long currentValue = redisTemplate.opsForValue().increment(originIP);
      if (currentValue == 1) {
        redisTemplate.expire(originIP, Duration.ofHours(1));
      }
      connection.exec();
      return null;
    });
  }
}
