package com.autodb.ops.dms.common.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CacheTest {
    @Test
    public void testCache(){
        List<Integer> list = Lists.newArrayList();
        list.add(2);
        Set<Integer> sets = Sets.newHashSet();
        sets.add(2);
        sets.add(4);
        sets.add(3);
        boolean fs = sets.contains(3);
        System.out.println(fs);
    }
}
