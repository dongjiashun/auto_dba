package com.autodb.ops.dms.common.util;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * @Description:拉取AD域账户
 * 
 * @ClassName: LdAPTest
 * @Copyright: Copyright (c) 2014
 * 
 * @author Comsys-LZP
 * @date 2014-8-7 上午10:20:22
 * @version V2.0
 */
public class LdAPTest {
	public static void main1(String[] args) {
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
			SearchControls searchCtls = new SearchControls();
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String searchFilter = "(&(objectCategory=person)(objectClass=user)(name=*))";
			String searchBase = "dc=navinfo,dc=com";
			String returnedAtts[] = {"memberOf"};
			searchCtls.setReturningAttributes(returnedAtts);
			NamingEnumeration<SearchResult> answer = ctx.search(searchBase, searchFilter,searchCtls);
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				System.out.println("<<<::[" + sr.getName()+"]::>>>>");
			}
			ctx.close();
		}catch (NamingException e) {
			e.printStackTrace();
			System.err.println("Problem searching directory: " + e);
		}
	}
}
