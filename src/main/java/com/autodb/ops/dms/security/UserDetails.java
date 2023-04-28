package com.autodb.ops.dms.security;

import com.autodb.ops.dms.entity.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * user details
 *
 * @author dongjs
 * @since 2015/11/5
 */
public class UserDetails extends User implements
        org.springframework.security.core.userdetails.UserDetails {
    private static final long serialVersionUID = 3047964176665864842L;

    private Collection<GrantedAuthority> authorities;

    public UserDetails(User user) {
        super(user);

        authorities = new HashSet<>();
        if (null != this.getRoles()) {
            this.getRoles().stream()
                    .filter(role -> null != role.getPrivileges())
                    .forEach(role -> authorities.addAll(role.getPrivileges().stream()
                            .map(privilege -> new SimpleGrantedAuthority(privilege.getCode()))
                            .collect(Collectors.toList())));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
