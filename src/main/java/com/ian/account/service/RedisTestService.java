package com.ian.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTestService {
    private final RedissonClient redissonClient;

    public String getLock() {
        RLock lock = redissonClient.getLock("sanmpleLock");

        try {
            boolean isLock = lock.tryLock(1, 3, TimeUnit.SECONDS);

            if (!isLock) {
                return "Lock fail";
            }
        } catch (InterruptedException e) {
            log.error("Redis lock error", e);
        }
        return "success";
    }
}
