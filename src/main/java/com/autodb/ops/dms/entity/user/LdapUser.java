package com.autodb.ops.dms.entity.user;
//ldap 用户数据
public class LdapUser {
	private String cn;
	private String sn;
	private String mail;
	private String mobile;
	
	
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getCn() {
		return cn;
	}
	public void setCn(String cn) {
		this.cn = cn;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	@Override
	public String toString() {
		return "LdapUser [cn=" + cn + ", sn=" + sn + ", mail=" + mail
				+ ", mobile=" + mobile + "]";
	}

	
	

}
