package com.example.ratelimiter.controllers;

import com.example.ratelimiter.services.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

  @Autowired
  private RateLimitService rateLimitService;

  @GetMapping(value = "/redis")
  public ResponseEntity<String> redisEndpoint(HttpServletRequest request) {
    // 1)  First, let's check if the service is already limited (throttled)
    String remoteAddr = request.getRemoteAddr();
    System.out.println("New request received for redis endpoint: " + remoteAddr);
    if (rateLimitService.isThrottled(remoteAddr)) {
      // If so, we'll return a HTTP status 429, which is used for rate limit
      return ResponseEntity
          .status(HttpStatus.TOO_MANY_REQUESTS)
          .header("x-rate-limit-reset",
              rateLimitService.getExpireInSeconds(remoteAddr).toString())
          .build();
    } else {
      // 2)  We are not limited, so we just increment the amount of requests
      rateLimitService.incrementAndThrottle(remoteAddr);

      // And then we can run our endpoint's actual logic
      try { Thread.sleep(10000); } catch(InterruptedException e){}

      return ResponseEntity.ok("OK!");
    }
  }

  @GetMapping(value = "/noRedis")
  public ResponseEntity<String> noRedisEndpoint(HttpServletRequest request) {
    // 1)  First, let's check if the service is already limited (throttled)
    String remoteAddr = request.getRemoteAddr();
    System.out.println("New request received for noRedis endpoint: " + remoteAddr);
    try { Thread.sleep(10000); } catch(InterruptedException e){}
    return ResponseEntity.ok("OK!");
  }
}
