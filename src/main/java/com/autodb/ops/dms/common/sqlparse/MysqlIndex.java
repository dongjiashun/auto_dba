package com.autodb.ops.dms.common.sqlparse;

import com.autodb.ops.dms.common.util.StringUtil;

import java.util.Iterator;
import java.util.List;

/**
 * Created by wuying on 17/1/13.
 */
public class MysqlIndex implements  Comparable {
    //indexType has 3 elements: primary,unique,ordinary;
    private String indexType;
    private List<String> info;

    public MysqlIndex(String indexType, List<String> info) {
        this.indexType = indexType;
        this.info = info;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public List<String> getInfo() {
        return info;
    }

    public void setInfo(List<String> info) {
        this.info = info;
    }

    //唯一索引和普通索引的重复比较方法
    public boolean Duplicated2(Object o){
        MysqlIndex mysqlIndex = (MysqlIndex) o;
        Iterator<String> iterator_o = mysqlIndex.getInfo().iterator();
        Iterator<String> iterator_t = this.info.iterator();
        while(iterator_o.hasNext()){
            String  a = iterator_o.next();

            //iterator_t 已经比完了
            if(! iterator_t.hasNext()){
                return  true;
            }
            String b = iterator_t.next();
            //相等则进入下一轮比较
            if(StringUtil.equalsIgnoreCase(a,b)){
                continue;
            }else {
                //不相等则索引不重复
                return false;
            }
        }
        //iterator_o 比完了则为重复索引
        return true;
    }


    //索引比较方法,包含主键索引的比较方法
    public  boolean Duplicated(Object o){
        MysqlIndex that = (MysqlIndex) o;
        if(this.indexType.equals("primary") ){

            if(that.info.containsAll(this.info)) {
                return true;
            }else {
                return  false;
            }
        }else  if(that.indexType.equals("primary")){

            if(this.info.containsAll(that.info)) {
                return true;
            }else {
                return  false;
            }
        }else {
            if(that.info.containsAll(this.info) || this.info.containsAll(that.info)){
                return this.Duplicated2(that);
            }else {
                return  false;
            }
        }
    }

    @Override
    public int compareTo(Object o) {

        MysqlIndex oo = (MysqlIndex) o;
        if(this.info.size() > oo.info.size() ){
            return  1;
        }else if(this.info.size() == oo.info.size() ){
            if(this.indexType == "primary"){
                return -1;
            }else if(this.indexType == "unique"){

                if(oo.indexType=="primary"){
                    return  1;
                }else {
                    return -1;
                }
            }else {
                return 1;
            }
        }else {
            return  -1;
        }
    }

    @Override
    public String toString() {
        return "(类型='" + indexType + '\'' +
                ", 相关列=" + info+")";
    }
}
