package tn.finix.ledgerengine.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class IdempotencyGuard {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "idempotency:ref:";
    private static final Duration TTL = Duration.ofHours(24);

    public IdempotencyGuard(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAlreadyProcessed(String referenceId) {
        if (redisTemplate == null) return false;
        try {
            Boolean exists = redisTemplate.hasKey(KEY_PREFIX + referenceId);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("Redis unavailable for idempotency check (falling through to DB): {}", e.getMessage());
            return false;
        }
    }

    public void markProcessed(String referenceId) {
        if (redisTemplate == null) return;
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + referenceId, "1", TTL);
        } catch (Exception e) {
            log.warn("Failed to mark idempotency key in Redis (non-critical): {}", e.getMessage());
        }
    }
}
