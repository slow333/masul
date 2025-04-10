package kr.masul.client.redisCache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheClient {

   private final StringRedisTemplate redisTemplate;

   public RedisCacheClient(StringRedisTemplate redisTemplate) {
      this.redisTemplate = redisTemplate;
   }

   public void set(String key, String value, long timeout, TimeUnit timeUnit) {
      redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
   }

   public String get(String key) {
      return redisTemplate.opsForValue().get(key);
   }

   public void delete(String key) {
      redisTemplate.delete(key);
   }

   public boolean isUserTokenInWhiteList(String userId, String tokenFromRequest ){
      String tokenFromRedis = get("whiteList:" + userId);
      return tokenFromRedis != null && tokenFromRedis.equals(tokenFromRequest);
   }
}
