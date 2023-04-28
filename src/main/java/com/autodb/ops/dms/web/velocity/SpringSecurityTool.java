package com.autodb.ops.dms.web.velocity;

import com.autodb.ops.dms.common.DmsWebContext;
import com.autodb.ops.dms.entity.user.Privilege;
import com.autodb.ops.dms.entity.user.Role;
import com.autodb.ops.dms.entity.user.User;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.SkipSetters;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security toolbox
 *
 * @author dongjs
 * @since 2015/11/6
 */
@DefaultKey("sec")
@SkipSetters
@ValidScope("request")
public class SpringSecurityTool extends SafeConfig {
    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * has Authority
     */
    public boolean has(String authority) {
        User user = DmsWebContext.get().getUser();
        for(Role role : user.getRoles()){
            for(Privilege p : role.getPrivileges()){
                if (authority.equals(p.getCode())) {
                    return true;
                }
            }
        }
        Authentication authentication = this.getAuthentication();
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (authority.equals(auth.getAuthority())) {
                return true;
            }
        }

        return false;
    }


    /**
     * has all authorities
     */
    public boolean all(String... authorities) {
        Set<String> userAuthorities = getUserAuthorities();
        for (String auth : authorities) {
            if (userAuthorities.contains(auth)) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * has any authorities
     */
    public boolean any(String... authorities) {
        Set<String> userAuthorities = getUserAuthorities();
        for (String auth : authorities) {
            if (userAuthorities.contains(auth)) {
                return true;
            }
        }
        return false;
    }

    /**
     * none authorities
     */
    public boolean none(String... authorities) {
        Set<String> userAuthorities = getUserAuthorities();
        for (String auth : authorities) {
            if (userAuthorities.contains(auth)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasRole(String role) {
        return has(ROLE_PREFIX + role);
    }

    public boolean allRoles(String... roles) {
        String[] authorities = getAuthorities(roles);
        return all(authorities);
    }

    public boolean anyRoles(String roles) {
        String[] authorities = getAuthorities(roles);
        return any(authorities);
    }

    public boolean noneRoles(String roles) {
        String[] authorities = getAuthorities(roles);
        return none(authorities);
    }

    private String[] getAuthorities(String... roles) {
        String[] authorities = new String[roles.length];
        for (int i = 0; i < roles.length; i++) {
            authorities[i] = ROLE_PREFIX + roles[i];
        }
        return authorities;
    }

    public boolean isAuthenticate() {
        return this.getAuthentication().isAuthenticated();

    }

    public Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        Objects.requireNonNull(context);
        Authentication authentication = context.getAuthentication();
        Objects.requireNonNull(authentication);
        return authentication;
    }

    public Object getPrincipal() {
        return this.getAuthentication().getPrincipal();
    }

    private Set<String> getUserAuthorities() {
        Set<String> authoritiesSet = new HashSet<>();
        Collection<? extends GrantedAuthority> authorities = this.getAuthentication().getAuthorities();
        authoritiesSet.addAll(authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
        return authoritiesSet;
    }
}