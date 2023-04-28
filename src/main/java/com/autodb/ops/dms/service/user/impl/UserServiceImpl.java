package com.autodb.ops.dms.service.user.impl;

import com.dianwoba.springboot.webapi.WebApiResponse;
import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.util.LdapbyUser;
import com.autodb.ops.dms.domain.staff.StaffService;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.user.UserDao;
import com.autodb.ops.dms.service.user.UserService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * user service
 * @author dongjs
 * @since 2015/11/10
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private StaffService staffService;

    @Override
    public User findByUsername(String username) throws AppException {
        return userDao.findByUsername(username);
    }

    @Override
    public List<User> findLikeUsername(String username) throws AppException {
        return userDao.findLikeUsername(username);
    }

    @Override
    public List<User> find(String query, Page<User> page) throws AppException {
        List<User> users = StringUtils.isBlank(query) ? userDao.findAll(page) : userDao.findLikeAll(query, page);
        page.setData(users);
        return users;
    }

    @Override
    public User findOrAdd(String userCode) throws AppException {
//    	//初始化ldap用户数据
//    	//基准位置
//		String dn = "DC=hzdomain1,DC=com";
//		//查询过滤条件
//		String username = "sAMAccountName="+userCode;
//	    LdapbyUser ldap = new LdapbyUser(dn, username);
	    
	    //判断用户有效期
	    
		//--------------------------
        User user = userDao.findByUsername(userCode);
        User usernew = new User();
        //ldap用户和mysql中用户表数据不一致，需要同步
        if(user==null){
        	 usernew.setNickname(userCode);
        	 usernew.setUsername(userCode);
        	 usernew.setEmail("");
        	 usernew.setMobile("");
        	 usernew.setGmtCreate(new Date());
        	userDao.add(usernew);
        	userDao.injectRoles(usernew);
        	//更新用户邮箱和姓名信息
//        	userDao.updateLdapInfo(ldap.ldapuser);
        	 return usernew;
        }else{
        	userDao.injectRoles(user);
        	//更新用户邮箱和姓名信息
//        	userDao.updateLdapInfo(ldap.ldapuser);
        	 return user;
        }
    }


    @Override
    @Transactional
    public void updateRoles(int userId, List<Integer> roles) throws AppException {
        User user = userDao.find(userId);
        if (user != null) {
            userDao.updateRoles(user, roles);
        }
    }
}
