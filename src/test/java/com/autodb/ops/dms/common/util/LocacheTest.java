package com.autodb.ops.dms.common.util;

import com.autodb.ops.dms.common.cache.LocalCache;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class LocacheTest {
    @Test
    public void test(){
        LocalCache.put("key","value");
        try {
            Object vo = LocalCache.get("key", new Callable() {
                @Override
                public Object call() throws Exception {
                    LocalCache.put("key","value1");
                    return "newvalue";
                }
            });

            Object vo1 = LocalCache.get("key1", new Callable() {
                @Override
                public Object call() throws Exception {
                    LocalCache.put("key1","value1");
                    return "newvalue";
                }
            });

            Object vo2 = LocalCache.get("key1", new Callable() {
                @Override
                public Object call() throws Exception {
                    LocalCache.put("key1","value1");
                    return "newvalue";
                }
            });
            System.out.println(vo + "\t"+vo1+ "\t"+vo2);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
