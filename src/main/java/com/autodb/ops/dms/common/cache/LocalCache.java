package com.autodb.ops.dms.common.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LocalCache {

    private static Logger logger = LoggerFactory.getLogger(LocalCache.class);

    private static Cache<String, Object> cache = CacheBuilder.newBuilder().recordStats()
            .concurrencyLevel(4)
            .maximumSize(100000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    public static Object get(String key, Callable callBack) throws ExecutionException{
        return cache.get(key,callBack);
    }

    public static void put(String key,Object object){
        cache.put(key,object);
        writeCacheStat();
    }

    public static void writeCacheStat(){
        logger.info(cache.stats().toString());
    }
}
