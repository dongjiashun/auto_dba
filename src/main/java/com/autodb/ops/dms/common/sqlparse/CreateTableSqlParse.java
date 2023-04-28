package com.autodb.ops.dms.common.sqlparse;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUnique;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.rule.SecretRule;
import com.autodb.ops.dms.common.util.CollectionUtil;
import com.autodb.ops.dms.common.util.StringUtil;

import java.util.*;

/**
 * Created by wuying on 17/1/13.
 */
public class CreateTableSqlParse {
    private MySqlStatementParser mySqlStatementParser;
    private MySqlCreateTableStatement create;
    private List<SQLTableElement> sqlTableElementList;
    private List<MysqlIndex> indexList = new ArrayList<>();
    private List<MysqlColumn> columnList = new ArrayList<>();
    private String tableName ;


    public CreateTableSqlParse(String sql) {

        mySqlStatementParser = new MySqlStatementParser(sql);
        create = (MySqlCreateTableStatement) mySqlStatementParser.parseCreateTable();
        tableName = create.getName().getSimpleName();
        sqlTableElementList=create.getTableElementList();

    }

    public void parse(){
        for(SQLTableElement ste : sqlTableElementList){
            if(ste instanceof SQLColumnDefinition) {
                //MySqlSQLColumnDefinition scd = (MySqlSQLColumnDefinition) ste;
                SQLColumnDefinition scd = (SQLColumnDefinition) ste;

                String name = StringUtil.removeBackQuote(scd.getName().getSimpleName()).trim();
                String comment = StringUtil.removeSingleQuote(scd.getComment().toString()).trim();

                //获取默认值
                SQLExpr defaults = scd.getDefaultExpr();

                //获取字段的数据类型
                SQLDataType dataType = scd.getDataType();
                String dataTypeName = dataType.getName();

                //类型大小信息,字符集,校对集等信息
                List<SQLExpr> dataTypeArguments  =dataType.getArguments();
                columnList.add(new MysqlColumn(name,comment,dataTypeName));

            }else if (ste instanceof MySqlKey){
                if (ste instanceof MySqlPrimaryKey){
                    MySqlPrimaryKey mpk = (MySqlPrimaryKey) ste;
                    indexList.add(new MysqlIndex("primary",listDeal(mpk.getColumns())));
                } else if (ste instanceof MySqlUnique){
                    MySqlUnique mu = (MySqlUnique) ste;
                    indexList.add(new MysqlIndex("unique",listDeal(mu.getColumns())));
                }else {
                    MySqlKey mk = (MySqlKey) ste;
                    indexList.add(new MysqlIndex("ordinary",listDeal(mk.getColumns())));
                }
            }else {
                System.out.print("not recognized element" + ste);
            }

        }


    }

    public Map<String,String> getCommentColumnMap(){
        Map<String,String> commentColumnMap=new HashMap<String,String>();
        for(MysqlColumn mc: columnList){
            commentColumnMap.put(mc.getComment(),mc.getName());
        }
        return commentColumnMap;
    }

    public List<MysqlIndex> getIndexList(){
        return indexList;
    }

    public List<MysqlColumn> getColumnList(){
        return columnList;
    }

    private List<String> listDeal(List list){
        return new ArrayList<String>(CollectionUtil.removeListBackQuate(list));
    }

    public Pair<Boolean, String> dupicateIndexCheck(){
        Boolean checkResult=true;
        StringBuffer  messages= new StringBuffer();
        messages.append(tableName).append(":\n");
        MysqlIndex mysqlIndices[] = new MysqlIndex[indexList.size()];
        int i =0;
        for(MysqlIndex mi :indexList){
            mysqlIndices[i]=mi;
            i++;
        }
        for(int x =0;x<=mysqlIndices.length-2;x++) {
            for (int y = x + 1; y <= mysqlIndices.length - 1; y++) {

                if (mysqlIndices[x].Duplicated(mysqlIndices[y])) {
                    checkResult = false;
                    messages.append("索引 ").append(mysqlIndices[x]).append(" 和索引 ").append(mysqlIndices[y]).append("重复了\n");
                }
            }
        }
        return Pair.of(checkResult,messages.toString());

    }

    public Pair<Boolean,String> secretRuleCheck(Map<String,String> commentColumnMap){

        SecretRule secretRule = new SecretRule();
        secretRule.init();
        //Pair<Boolean,String> retPair = secretRule.check(commentColumnMap);
        Pair<Boolean,String> secretPair = secretRule.mapContainsCheck(commentColumnMap);
        String s=tableName.concat(":\n").concat(secretPair.getRight());
        return Pair.of(secretPair.getLeft(),s);
    }


    public static void main1(String[] args) throws  Exception {


        String sql="CREATE TABLE `bbs_tag` (\n" +
                "  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',\n" +
                "  `abc_realnam` varchar(32) NOT NULL COMMENT '银行姓名',\n" +
                "  `bank_account` tinyint(4) unsigned NOT NULL DEFAULT '1' COMMENT '账号',\n" +
                "  `subscribe_count` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '订阅人数',\n" +
                "  `digest_count` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '精华帖数',\n" +
                "  `created_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',\n" +
                "  `updated_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `uk_name` (name,`bank_account`,`id`),\n" +
                "  UNIQUE KEY `uk_name_1` (name,id,`bank_account`,`updated_time`),\n" +
                "   Key idx_digest_count(`digest_count`),\n"+
                "   Key idx_digest_count(`digest_count`,`updated_time`)\n"+
                ") ENGINE=InnoDB AUTO_INCREMENT=20003 DEFAULT CHART=utf8 comment 'abc';";



        CreateTableSqlParse test = new CreateTableSqlParse(sql);
        test.parse();
        Map commentColumnMap = test.getCommentColumnMap();

        ArrayList<MysqlIndex> mil= (ArrayList<MysqlIndex>) test.getIndexList();
        Collections.sort(mil);
        //System.out.println(mil);

        System.out.println("===================");


        Pair<Boolean,String> secretPair = test.secretRuleCheck(commentColumnMap);

        System.out.println(secretPair.getLeft());
        System.out.println(secretPair.getRight());


        System.out.println("===================");
        Pair<Boolean,String>  duplicatePair=  test.dupicateIndexCheck();

        System.out.println(duplicatePair.getLeft());
        System.out.println(duplicatePair.getRight());



    }
}
