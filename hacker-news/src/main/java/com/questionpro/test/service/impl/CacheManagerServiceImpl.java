package com.questionpro.test.service.impl;

import com.questionpro.test.constants.AppConstants;
import com.questionpro.test.service.CacheManagerService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CacheManagerServiceImpl implements CacheManagerService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /*
     * @Author: Yash.joshi
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error occurred while fetching record from cache "+e.toString());
        }
        return null;
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value, 15, TimeUnit.MINUTES);
    }
   // clearing key - top-stories cache before server gets down
    @PreDestroy
    public void cleanupCache() {
        redisTemplate.delete(AppConstants.CACHE_TOP_STORIES);
    }
}
