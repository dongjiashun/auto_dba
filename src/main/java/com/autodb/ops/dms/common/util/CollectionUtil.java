package com.autodb.ops.dms.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuying on 17/1/13.
 */
public class CollectionUtil {
    public static List<String> removeListBackQuate(List list){

        List<String> retList=new ArrayList<String>();
        for(Object o :list){
            retList.add(StringUtil.removeBackQuote(o.toString()).trim());
        }
        return retList;
    }

    public static void main1(String[] args) {

        List<String> testList=new ArrayList<String>();
        testList.add("` sdfsdf`");
        testList.add("` dsdfsdf`");
        testList.add("` sdfsgf `");
        System.out.println(CollectionUtil.removeListBackQuate(testList));


    }
}
