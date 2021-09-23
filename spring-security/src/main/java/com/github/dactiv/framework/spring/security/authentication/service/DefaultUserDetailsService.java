package com.github.dactiv.framework.spring.security.authentication.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.config.DefaultUserProperties;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.RoleAuthority;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
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
public class DefaultUserDetailsService implements UserDetailsService, InitializingBean {

    public static final String DEFAULT_TYPES = "Default";

    private final AuthenticationProperties properties;

    private final PasswordEncoder passwordEncoder;

    private final RedissonClient redissonClient;

    public DefaultUserDetailsService(AuthenticationProperties properties,
                                     PasswordEncoder passwordEncoder,
                                     RedissonClient redissonClient) {
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
        this.redissonClient = redissonClient;
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
    public List<String> getType() {
        return Collections.singletonList(DEFAULT_TYPES);
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    @Override
    public void afterPropertiesSet()  {
        properties.getUsers().forEach(this::syncSecurityUserDetails);
    }

    private SecurityUserDetails getUser(String username) {

        DefaultUserProperties defaultUserProperties = properties
                .getUsers()
                .stream()
                .filter(u -> u.getData().stream().anyMatch(d -> d.getName().equals(username)))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("用户名密码错误"));

        RList<SecurityUserDetails> list = redissonClient.getList(defaultUserProperties.getCache().getName());

        return list
                .stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("用户名密码错误"));
    }

    private boolean isUserEquals(SecurityProperties.User user, SecurityUserDetails details) {

        List<String> detailsAuthorities = details
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(s -> StringUtils.remove(s, RoleAuthority.DEFAULT_ROLE_PREFIX))
                .collect(Collectors.toList());

        return user.getName().equals(details.getUsername())
                && matchesPassword(user.getPassword(),null, details)
                && user.getRoles().containsAll(detailsAuthorities);
    }

    private void syncSecurityUserDetails(DefaultUserProperties properties) {

        CacheProperties cache = properties.getCache();

        RList<SecurityUserDetails> list = redissonClient.getList(cache.getName());

        if (Objects.nonNull(cache.getExpiresTime())) {
            list.expireAsync(
                    cache.getExpiresTime().getValue(),
                    cache.getExpiresTime().getUnit()
            );
        }

        for (SecurityProperties.User user : properties.getData()) {

            Optional<SecurityUserDetails> optional = list
                    .stream()
                    .filter(s -> user.getName().equals(s.getUsername()))
                    .findFirst();

            if (optional.isPresent()) {

                SecurityUserDetails redisUser = optional.get();

                if (isUserEquals(user, redisUser)) {
                    continue;
                }

                list.removeAsync(redisUser);
            }

            SecurityUserDetails newOne = new SecurityUserDetails(
                    UUID.randomUUID().toString(),
                    user.getName(),
                    passwordEncoder.encode(user.getPassword())
            );

            if (CollectionUtils.isNotEmpty(user.getRoles())) {
                List<RoleAuthority> authorities = user
                        .getRoles()
                        .stream()
                        .map(RoleAuthority::new)
                        .collect(Collectors.toList());

                newOne.setRoleAuthorities(authorities);
            }

            newOne.setType(DEFAULT_TYPES);

            list.addAsync(newOne);
        }
    }
}
