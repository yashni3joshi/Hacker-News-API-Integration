package com.questionpro.test.service;

public interface CacheManagerService {
     Object get(String key);
     void set(String key, String value);
}
