package com.autodb.ops.dms.repository.user.impl;

import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.entity.user.LdapUser;
import com.autodb.ops.dms.entity.user.Role;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.repository.SuperDao;
import com.autodb.ops.dms.repository.user.RoleDao;
import com.autodb.ops.dms.repository.user.UserDao;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * user dao
 * @author dongjs
 * @since 2015/11/9
 */
@Repository
public class UserDaoImpl extends SuperDao implements UserDao {
    @Autowired
    private RoleDao roleDao;

//    查询账号过期用户
	@Override
	public User useroverdue(String username) throws AppException {
		//查询账号过期用户
		return this.getSqlSession().selectOne("UserMapper.findUserOverdue", username);
	}
    @Override
    public User find(int id) throws AppException {
        return this.getSqlSession().selectOne("UserMapper.find", id);
    }

    @Override
    public User findByUsername(String username) throws AppException {
        return this.getSqlSession().selectOne("UserMapper.findByUsername", username);
    }
    
    @Override
    public User findByNickname(String nickname) throws AppException {
        return this.getSqlSession().selectOne("UserMapper.findByNickname", nickname);
    }
    @Override
    public User findByEmail(String email) throws AppException {
        return this.getSqlSession().selectOne("UserMapper.findByEmail", email);
    }

    @Override
    public List<User> findByRole(String code) throws AppException {
        return this.getSqlSession().selectList("UserMapper.findByRoleCode", code);
    }

    @Override
    public List<User> findByUsernames(List<String> usernames) throws AppException {
        return this.getSqlSession().selectList("UserMapper.findByUsernames", usernames);
    }

    @Override
    public List<User> findLikeUsername(String username) throws AppException {
        return this.getSqlSession().selectList("UserMapper.findLikeUsername", username + '%');
    }

    @Override
    public List<User> findAll(Page<User> page) throws AppException {
        page.pagination.setRowCount((int) this.selectCount("UserMapper.findAll"));
        return this.getSqlSession().selectList("UserMapper.findAll", Collections.emptyMap(),
                new RowBounds(page.pagination.getOffset(), page.pagination.getLimit()));
    }

    @Override
    public List<User> findLikeAll(String username, Page<User> page) throws AppException {
        username = username + '%';

        page.pagination.setRowCount((int) this.selectCount("UserMapper.findLikeUsername", username));
        return this.getSqlSession().selectList("UserMapper.findLikeUsername", username,
                new RowBounds(page.pagination.getOffset(), page.pagination.getLimit()));
    }

    @Override
    public Map<Integer, User> findMap(List<Integer> ids) throws AppException {
        if (ids.size() < 1) {
            return Collections.emptyMap();
        }
        return this.getSqlSession().selectMap("UserMapper.findByIds", ids, "id");
    }

    @Override
    public void add(User user) throws AppException {
        this.getSqlSession().insert("UserMapper.add", user);
    }

    @Override
    public void updateRoles(User user, List<Integer> roles) throws AppException {
        this.getSqlSession().delete("UserMapper.deleteRoles", user.getId());

        if (roles.size() > 0) {
            Map<Integer, Role> roleMap = roleDao.findPureAll().stream()
                    .collect(Collectors.toMap(Role::getId, role -> role));
            roles = roles.stream()
                    .map(roleMap::get)
                    .filter(role -> role != null)
                    .map(Role::getId)
                    .collect(Collectors.toList());

            if (roles.size() > 0) {
                Map<String, Object> params = new HashMap<>();
                params.put("userId", user.getId());
                params.put("roles", roles);
                this.getSqlSession().insert("UserMapper.addRoles", params);
            }
        }
    }

    @Override
    public void injectRoles(User user) throws AppException {
        List<Role> roles = roleDao.findByUser(user.getId());
        user.setRoles(roles);
    }

	@Override
	public boolean updateLdapInfo(LdapUser user) throws AppException {
		  return this.getSqlSession().update("UserMapper.updateladp", user) > 0;
		
	}


    
}
