package com.autodb.ops.dms.common.rule;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wuying on 17/1/13.
 */
public class SecretRule {


    private Map<String,String> ruleMap = new HashMap<String,String>();

    public SecretRule() {


    }

    public void init(){
      /*  this.ruleMap.put("用户id","uid");
        this.ruleMap.put("账号","account");
        this.ruleMap.put("手机号","mobile");
        this.ruleMap.put("姓名","realname");
        this.ruleMap.put("邮箱","email");
        this.ruleMap.put("身份证","idno");
        this.ruleMap.put("银行卡","cardno");
        this.ruleMap.put("地址","address");*/
    }


    public Pair<Boolean, String> check(Map<String,String > commentcolumnMap){


        Boolean rule = true;
        StringBuffer messages = new StringBuffer();
        for(Map.Entry<String,String> commentcolumn :commentcolumnMap.entrySet() ){
            String comment = commentcolumn.getKey();
            String column  = commentcolumn.getValue();
            if(this.ruleMap.containsKey(comment)){
                if(! StringUtil.equals(this.ruleMap.get(comment),column) ){
                    rule =false;
                    messages.append("[sensitive column rule]:comment " + comment + " column must be " + this.ruleMap.get(comment)+" not "+ column + "!!!\n");
                }
            }
        }

        return new Pair<Boolean, String>(rule,messages.toString());
    }


    public Pair<Boolean,String> mapContainsCheck(Map<String ,String> commentColumnMap){

        Boolean ret=true;
        StringBuffer messages = new StringBuffer();

        Set<String> ruleSet = ruleMap.keySet();
        Set<String> commentSet = commentColumnMap.keySet();
        for(String comment : commentSet){

            for(String rule:ruleSet){
                if(comment.contains(rule)){
                    String ruleColumn= ruleMap.get(rule);
                    String commentColumn= commentColumnMap.get(comment);
                    if(commentColumn.contains(ruleColumn)){
                        continue;
                    }else{

                        ret=false;
                        messages.append("含有注释'")
                                .append(comment)
                                .append("'的列")
                                .append("命名规范必须包含")
                                .append("`")
                                .append(ruleColumn)
                                .append("`关键字")
                                .append("而不能为")
                                .append("`")
                                .append(commentColumn)
                                .append("`\n");
                    }

                }

            }
        }

        return new Pair(ret,messages.toString());

    }

}