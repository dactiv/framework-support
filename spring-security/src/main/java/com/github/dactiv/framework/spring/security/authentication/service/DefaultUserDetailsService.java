package com.github.dactiv.framework.spring.security.authentication.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.security.entity.RoleAuthority;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认用户明细实现
 *
 * @author maurice.chen
 */
public class DefaultUserDetailsService implements UserDetailsService {

    public static final String DEFAULT_TYPES = "Default";

    private final Map<String, SecurityUserDetails> userDetailsMap = new LinkedHashMap<>();

    private final PasswordEncoder passwordEncoder;

    public DefaultUserDetailsService(AuthenticationProperties properties,
                                     PasswordEncoder passwordEncoder) {

        for (SecurityProperties.User user : properties.getUsers()) {

            SecurityUserDetails userDetails = new SecurityUserDetails(
                    UUID.randomUUID().toString(),
                    user.getName(),
                    passwordEncoder.encode(user.getPassword())
            );

            userDetails.setRoleAuthorities(
                    user.getRoles().stream().map(RoleAuthority::new).collect(Collectors.toList())
            );

            userDetailsMap.put(userDetails.getUsername(), userDetails);
        }

        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token) throws AuthenticationException {
        return getUser(token.getPrincipal().toString());
    }

    @Override
    public Collection<? extends GrantedAuthority> getPrincipalAuthorities(SecurityUserDetails userDetails) {
        return userDetails.getAuthorities();
    }

    @Override
    public CacheProperties getAuthenticationCache(PrincipalAuthenticationToken token) {
        return null;
    }

    @Override
    public CacheProperties getAuthorizationCache(PrincipalAuthenticationToken token) {
        return null;
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(DEFAULT_TYPES);
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    private SecurityUserDetails getUser(String username) {

        SecurityUserDetails userDetails = userDetailsMap.get(username);

        if (Objects.isNull(userDetails)) {
            throw new BadCredentialsException("用户名密码错误");
        }

        return userDetails;
    }

}
