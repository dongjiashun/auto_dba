package com.autodb.ops.dms.common.util;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import com.autodb.ops.dms.entity.user.LdapUser;

/**
 * Java通过Ldap操作AD的增删该查询
 * 
 * @author guob
 */

public class LdapbyUser {
	public static LdapUser ldapuser = new LdapUser();
	DirContext dc = null;
	String root = "dc=htrader,dc=cn"; // LDAP的根节点的DC

	/**
	 * 
	 * @param dn类似于
	 *            "CN=RyanHanson,dc=example,dc=com"
	 * @param employeeID是Ad的一个员工号属性
	 */
	public LdapbyUser(String dn, String username) {
		init();
		// add();//添加节点
		// delete("ou=hi,dc=example,dc=com");//删除"ou=hi,dc=example,dc=com"节点
		// renameEntry("ou=new,o=neworganization,dc=example,dc=com","ou=neworganizationalUnit,o=neworganization,dc=example,dc=com");//重命名节点"ou=new,o=neworganization,dc=example,dc=com"
		 searchInformation(dn, "",
				 username);//遍历所有根节点
//		modifyInformation(dn, employeeID);// 修改
		// Ldapbyuserinfo("guob");//遍历指定节点的分节点
		close();
	}

	/**
	 * 
	 * Ldap连接
	 * 
	 * @return LdapContext
	 */
	public void init() {
		Hashtable env = new Hashtable();
		String LDAP_URL = "ldap://10.0.1.101:389"; // LDAP访问地址
		String adminName = "hzdongjiashun@hzdomain1.com"; // 注意用户名的写法：domain\User或
		String adminPassword = "1212ming"; // 密码
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, LDAP_URL);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, adminName);
		env.put(Context.SECURITY_CREDENTIALS, adminPassword);
		try {
			dc = new InitialDirContext(env);// 初始化上下文
			System.out.println("认证成功");// 这里可以改成异常抛出。
		} catch (javax.naming.AuthenticationException e) {
			System.out.println("认证失败");
		} catch (Exception e) {
			System.out.println("认证出错：" + e);
		}
	}

	/**
	 * 添加
	 */
	public void add(String newUserName) {
		try {
			BasicAttributes attrs = new BasicAttributes();
			BasicAttribute objclassSet = new BasicAttribute("objectClass");
			objclassSet.add("sAMAccountName");
			objclassSet.add("employeeID");
			attrs.put(objclassSet);
			attrs.put("ou", newUserName);
			dc.createSubcontext("ou=" + newUserName + "," + root, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in add():" + e);
		}
	}

	/**
	 * 删除
	 * 
	 * @param dn
	 */
	public void delete(String dn) {
		try {
			dc.destroySubcontext(dn);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in delete():" + e);
		}
	}

	/**
	 * 重命名节点
	 * 
	 * @param oldDN
	 * @param newDN
	 * @return
	 */
	public boolean renameEntry(String oldDN, String newDN) {
		try {
			dc.rename(oldDN, newDN);
			return true;
		} catch (NamingException ne) {
			System.err.println("Error: " + ne.getMessage());
			return false;
		}
	}

	/**
	 * 修改
	 * 
	 * @return
	 */
	public boolean modifyInformation(String dn, String employeeID) {
		try {
			System.out.println("updating...\n");
			ModificationItem[] mods = new ModificationItem[1];
			/* 修改属性 */
			// Attribute attr0 = new BasicAttribute("employeeID", "W20110972");
			// mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
			// attr0);

			/* 删除属性 */
			// Attribute attr0 = new BasicAttribute("description",
			// "陈轶");
			// mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
			// attr0);
//			objectClass: person
			/* 添加属性 */
			Attribute attr0 = new BasicAttribute("employeeID", employeeID);
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr0);
			/* 修改属性 */
			dc.modifyAttributes(dn + ",dc=example,dc=com", mods);
			return true;
		} catch (NamingException e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
			return false;
		}
	}

	/**
	 * 关闭Ldap连接
	 */
	public void close() {
		if (dc != null) {
			try {
				dc.close();
			} catch (NamingException e) {
				System.out.println("NamingException in close():" + e);
			}
		}
	}

	/**
	 * @param base
	 *            ：根节点(在这里是"dc=example,dc=com")
	 * @param scope
	 *            ：搜索范围,分为"base"(本节点),"one"(单层),""(遍历)
	 * @param filter
	 *            ：指定子节点(格式为"(objectclass=*)",*是指全部，你也可以指定某一特定类型的树节点)
	 */
	public void searchInformation(String base, String scope, String filter) {
		
		String returnedAtts[] = { "uid","cn","sn", "mail","telephoneNumber","userPrincipalName","sAMAccountName" };
		SearchControls sc = new SearchControls();
		if (scope.equals("base")) {
			sc.setSearchScope(SearchControls.OBJECT_SCOPE);
		} else if (scope.equals("one")) {
			sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		} else {
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
		}
		NamingEnumeration ne = null;
		try {
			ne = dc.search(base, filter, sc);
			// Use the NamingEnumeration object to cycle through
			// the result set.
			while (ne.hasMore()) {
				SearchResult sr = (SearchResult) ne.next();
				System.out.println(sr.toString());
				String name = sr.getName();
				if (base != null && !base.equals("")) {
				
					System.out.println("entry: " + name + "," + base);
				} else {
					System.out.println("entry: " + name);
				}

				Attributes at = sr.getAttributes();
				NamingEnumeration ane = at.getAll();
				while (ane.hasMore()) {
					Attribute attr = (Attribute) ane.next();
					String attrType = attr.getID();
					List<String> ldapparam = Arrays.asList(returnedAtts);
					if(!ldapparam.contains(attrType)){
						continue;
					}
					NamingEnumeration values = attr.getAll();
					Vector vals = new Vector();
					// Another NamingEnumeration object, this time
					// to iterate through attribute values.
					while (values.hasMore()) {
						Object oneVal = values.nextElement();
						if (oneVal instanceof String) {
							//添加信息到ldap信息中
							if("sAMAccountName".equals(attrType)){
								ldapuser.setSn(oneVal.toString());
							}else if("userPrincipalName".equals(attrType)){
								ldapuser.setMail(oneVal.toString());
							}else if("cn".equals(attrType)){
								ldapuser.setCn(oneVal.toString());
							}else if("telephoneNumber".equals(attrType)){
								ldapuser.setMobile(oneVal.toString());
							}
							System.out.println(attrType + ": "
									+ (String) oneVal);
						} else {
							System.out.println(attrType + ": "
									+ new String((byte[]) oneVal));
						}
					}
					
				}
			}
		} catch (Exception nex) {
			System.out.println("Error: " + nex.getMessage());
		}
		System.out.println(ldapuser);
	}

	/**
	 * 查询
	 * 
	 * @throws NamingException
	 */
	public void Ldapbyuserinfo(String userName) {
		// Create the search controls
		SearchControls searchCtls = new SearchControls();
		// Specify the search scope
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		// specify the LDAP search filter
		String searchFilter = "sn=" + userName;
		// Specify the Base for the search 搜索域节点
		String searchBase = "ou=People,dc=htrader,dc=cn";
		int totalResults = 0;
		String returnedAtts[] = { "uid","cn","sn", "mail" }; // 定制返回属性

		searchCtls.setReturningAttributes(returnedAtts); // 设置返回属性集

		// searchCtls.setReturningAttributes(null); // 不定制属性，将返回所有的属性集

		try {
			@SuppressWarnings("rawtypes")
			NamingEnumeration answer = dc.search(searchBase, searchFilter,
					searchCtls);
			if (answer == null || answer.equals(null)) {
				System.out.println("answer is null");
			} else {
				System.out.println("answer not null");
			}
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				System.out
						.println("************************************************");
				System.out.println("getname=" + sr.getName());
				Attributes Attrs = sr.getAttributes();
				if (Attrs != null) {
					try {

						for (NamingEnumeration ne = Attrs.getAll(); ne
								.hasMore();) {
							Attribute Attr = (Attribute) ne.next();
							System.out.println("AttributeID="
									+ Attr.getID().toString());
							// 读取属性值
							for (NamingEnumeration e = Attr.getAll(); e
									.hasMore(); totalResults++) {
								String user = e.next().toString(); // 接受循环遍历读取的userPrincipalName用户属性
								System.out.println(user);
							}
							// System.out.println(" ---------------");
							// // 读取属性值
							// Enumeration values = Attr.getAll();
							// if (values != null) { // 迭代
							// while (values.hasMoreElements()) {
							// System.out.println(" 2AttributeValues="
							// + values.nextElement());
							// }
							// }
							// System.out.println(" ---------------");
						}
					} catch (NamingException e) {
						System.err.println("Throw Exception : " + e);
					}
				}
			}
			System.out.println("Number: " + totalResults);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Throw Exception : " + e);
		}
	}

	/**
	 * 主函数用于测试
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//基准位置
		String dn = "DC=hzdomain1,DC=com";
		//查询过滤条件
//		String username = "sAMAccountName=hzlisha";
//		cn=李沙
		String username = "cn=董佳顺";
		new LdapbyUser(dn, username);
		
	}
}