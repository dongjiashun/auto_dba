package com.autodb.ops.dms.domain.sqlcheck.checks;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.sqlparse.CreateTableSqlParse;
import com.autodb.ops.dms.common.sqlparse.MysqlIndex;
import com.autodb.ops.dms.common.util.StringUtil;
import com.autodb.ops.dms.entity.datasource.DataSource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by wuying on 17/1/13.
 */
@Component
public class CreateTableSqlCheck  extends  AbstractSqlCheck{



    @Override
    public Optional<String> check(String sql, DataSource dataSource) {
        StringBuilder message = new StringBuilder();

        String[]  sqls = sql.split(";");

        //
        for(int i=0; i< sqls.length;i++){
            String sqlTmp = sqls[i].trim();
            if(StringUtil.match(sqlTmp.toUpperCase(),"^CREATE\\s+TABLE")){
                System.out.println("create table " + sqlTmp);
                CreateTableSqlParse test = new CreateTableSqlParse(sqlTmp);
                test.parse();
                Map commentColumnMap = test.getCommentColumnMap();
                Pair<Boolean,String> secretPair = test.secretRuleCheck(commentColumnMap);
                if(! secretPair.getLeft()){
                    message.append(secretPair.getRight());
                }
                Pair<Boolean,String>  duplicatePair=  test.dupicateIndexCheck();

                if(! duplicatePair.getLeft()){
                    message.append(duplicatePair.getRight());
                }
            }else {
                System.out.println("not create table " + sqlTmp);
                continue;
            }
        }


        if(message.length() == 0){
            System.out.println(message.toString());
            return   Optional.empty();
        }else {
            return Optional.of(message.toString());
        }
    }

}
