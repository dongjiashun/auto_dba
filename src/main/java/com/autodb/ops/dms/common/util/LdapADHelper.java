package com.autodb.ops.dms.common.util;
import java.util.Hashtable;  
import java.util.Properties;

import javax.naming.Context;  
import javax.naming.NamingEnumeration;  
import javax.naming.NamingException;  
import javax.naming.directory.Attribute;  
import javax.naming.directory.Attributes;  
import javax.naming.directory.SearchControls;  
import javax.naming.directory.SearchResult;  
import javax.naming.ldap.InitialLdapContext;  
import javax.naming.ldap.LdapContext;  
  
public class LdapADHelper {  
    public LdapADHelper() {  
    }  
    private String host,url,adminName,adminPassword;  
    private LdapContext ctx = null;  
    /** 
     * 初始化ldap 
     */  
    public void initLdap(){  
        //ad服务器  
		Properties env = new Properties();
		String adminName = "cn=admin,dc=navinfo,dc=com";//username@domain
		String adminPassword = "123456";//password
		String ldapURL = "ldap://192.168.171.129:389/";//ip:port
		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");//"none","simple","strong"
		env.put(Context.SECURITY_PRINCIPAL, adminName);
		env.put(Context.SECURITY_CREDENTIALS, adminPassword);
		env.put(Context.PROVIDER_URL, ldapURL);
		try {
			LdapContext ctx = new InitialLdapContext(env, null);
             System.out.println("初始化ldap成功！");  
        } catch (NamingException e) {  
            e.printStackTrace();  
            System.err.println("Throw Exception : " + e);  
        }  
    }  
    /** 
     * 关闭ldap 
     */  
    public void closeLdap(){  
        try {  
            this.ctx.close();  
        } catch (NamingException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
    }  
    /** 
     *  
     * @param type organizationalUnit:组织架构 group：用户组 user|person：用户 
     * @param name 
     * @return 
     */  
    public String GetADInfo(String type ,String filter ,String name) {  
  
        String userName = name; // 用户名称  
        if (userName == null) {  
            userName = "";  
        }  
        String company = "";  
        String result = "";  
        try {  
            // 域节点  
            String searchBase = "ou=People,dc=navinfo,dc=com";  
            // LDAP搜索过滤器类  
            //cn=*name*模糊查询 cn=name 精确查询  
          String searchFilter = "(objectClass="+type+")";  
//            String searchFilter = "(&(objectClass="+type+")("+filter+"=*" + name + "*))";  
            // 创建搜索控制器  
            SearchControls searchCtls = new SearchControls();   
            //  设置搜索范围  
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);   
//          String returnedAtts[] = {  "memberOf" }; // 定制返回属性  
//      searchCtls.setReturningAttributes(returnedAtts); // 设置返回属性集 不设置则返回所有属性  
            // 根据设置的域节点、过滤器类和搜索控制器搜索LDAP得到结果  
            NamingEnumeration answer = ctx.search(searchBase, searchFilter,searchCtls);// Search for objects using the filter  
            // 初始化搜索结果数为0  
            int totalResults = 0;// Specify the attributes to return  
            int rows = 0;  
            while (answer.hasMoreElements()) {// 遍历结果集  
                SearchResult sr = (SearchResult) answer.next();// 得到符合搜索条件的DN  
                ++rows;  
                String dn = sr.getName();  
                System.out.println(dn);  
                Attributes Attrs = sr.getAttributes();// 得到符合条件的属性集  
                if (Attrs != null) {  
                    try {  
                        for (NamingEnumeration ne = Attrs.getAll(); ne.hasMore();) {  
                            Attribute Attr = (Attribute) ne.next();// 得到下一个属性  
                            System.out.println(" AttributeID=属性名："+ Attr.getID().toString());  
                            // 读取属性值  
                            for (NamingEnumeration e = Attr.getAll(); e.hasMore(); totalResults++) {  
                                company = e.next().toString();  
                                System.out.println("    AttributeValues=属性值："+ company);  
                            }  
                            System.out.println("    ---------------");  
  
                        }  
                    } catch (NamingException e) {  
                        System.err.println("Throw Exception : " + e);  
                    }  
                }// if  
            }// while  
            System.out.println("************************************************");  
            System.out.println("Number: " + totalResults);  
            System.out.println("总共用户数："+rows);  
        } catch (NamingException e) {  
            e.printStackTrace();  
            System.err.println("Throw Exception : " + e);  
        }  
        return result;  
    }  
  
    public static void main(String args[]) {  
        // 实例化  
        LdapADHelper ad = new LdapADHelper();  
        ad.initLdap();  
    ad.GetADInfo("accout","cn","李XX");//查找用户  
//        ad.GetADInfo("organizationalUnit","ou","工程");//查找组织架构  
//    ad.GetADInfo("group","cn","福建xxx");//查找用户组  
          
        ad.closeLdap();  
    }  
}  