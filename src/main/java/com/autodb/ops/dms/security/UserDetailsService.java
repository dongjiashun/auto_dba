package com.autodb.ops.dms.security;

import com.google.common.base.Splitter;
import com.autodb.ops.dms.common.DmsWebContext;
import com.autodb.ops.dms.common.util.ServletUtils;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.service.user.UserService;

//import com.autodb.springboot.web.security.cas.autocfg.audit.Auditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * cas authentication UserDetailsService
 *
 * @author dongjs
 * @since 2015/11/5
 */
@Component
public class UserDetailsService implements
        org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserService userService;

   /* @Autowired
    private Auditor auditor;*/

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findOrAdd(email);
        /*String username = getUsername(email);
        User user = userService.findOrAdd(username, email);*/
        if (user == null) {
            throw new UsernameNotFoundException(email);	
        }
        DmsWebContext.set(DmsWebContext.of(user, "127.0.0.1"));
//        auditor.audit(user.getUsername(), "login");
        return new UserDetails(user);
    }

    private String getUsername(String email) {
        List<String> strings = Splitter.on('@').splitToList(email);
        return strings.size() > 0 ? strings.get(0) : email;
    }
}
