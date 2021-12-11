package com.github.dactiv.framework.spring.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.spring.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * spring security 用户实现
 *
 * @author maurice.chen
 */
public class SecurityUserDetails implements UserDetails {

    private static final long serialVersionUID = 1369484231035811533L;

    public static final String DEFAULT_IS_AUTHENTICATED_METHOD_NAME = "isAuthenticated";

    public static final String DEFAULT_HAS_ANY_ROLE_METHOD_NAME = "hasAnyRole";

    public static final String DEFAULT_HAS_ROLE_METHOD_NAME = "hasRole";

    public static final String DEFAULT_AUTHORITIES_FIELD_NAME = "authorities";

    public static final String DEFAULT_ROLE_AUTHORITIES_FIELD_NAME = "roleAuthorities";

    public static final String DEFAULT_RESOURCE_AUTHORITIES_FIELD_NAME = "resourceAuthorities";

    public static final List<String> DEFAULT_SUPPORT_SECURITY_METHOD_NAME = Arrays.asList(
            "hasAuthority",
            "hasAnyAuthority",
            DEFAULT_HAS_ROLE_METHOD_NAME,
            DEFAULT_HAS_ANY_ROLE_METHOD_NAME,
            DEFAULT_IS_AUTHENTICATED_METHOD_NAME
    );

    public static final List<String> DEFAULT_ROLE_PREFIX_METHOD_NAME = Arrays.asList(
            DEFAULT_HAS_ANY_ROLE_METHOD_NAME,
            DEFAULT_HAS_ROLE_METHOD_NAME
    );

    private Object id;

    @JsonIgnore
    private String password;
    private String username;
    @JsonIgnore
    private List<ResourceAuthority> resourceAuthorities = new ArrayList<>();
    private List<RoleAuthority> roleAuthorities = new ArrayList<>();
    @JsonIgnore
    private boolean accountNonExpired = true;
    @JsonIgnore
    private boolean accountNonLocked = true;
    @JsonIgnore
    private boolean credentialsNonExpired = true;
    private UserStatus status;
    private String type;

    public SecurityUserDetails() {
    }

    public SecurityUserDetails(Object id, String username, String password) {
        this(id, username, password, UserStatus.Enabled);
    }

    public SecurityUserDetails(Object id, String username, String password, UserStatus userStatus) {
        this(id, username, password, userStatus, true, true, true);
    }

    public SecurityUserDetails(Object id, String username, String password, UserStatus status,
                               boolean accountNonExpired, boolean credentialsNonExpired,
                               boolean accountNonLocked) {

        if (StringUtils.isBlank(username) || password == null) {
            throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
        }

        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> result = new ArrayList<>();

        result.addAll(
                resourceAuthorities.stream()
                        .filter(x -> StringUtils.isNotBlank(x.getAuthority()))
                        .filter(x -> !DEFAULT_IS_AUTHENTICATED_METHOD_NAME.equals(x.getAuthority()))
                        .flatMap(x -> Arrays.stream(StringUtils.split(x.getAuthority(), SpringMvcUtils.COMMA_STRING)))
                        .map(StringUtils::trimToEmpty)
                        .filter(StringUtils::isNotEmpty)
                        .filter(x -> StringUtils.startsWith(x, ResourceAuthority.DEFAULT_RESOURCE_PREFIX))
                        .filter(x -> StringUtils.endsWith(x, ResourceAuthority.DEFAULT_RESOURCE_SUFFIX))
                        .map(SimpleGrantedAuthority::new)
                        .distinct()
                        .collect(Collectors.toList())
        );

        result.addAll(
                roleAuthorities.stream()
                        .map(x -> RoleAuthority.DEFAULT_ROLE_PREFIX + x.getAuthority())
                        .map(SimpleGrantedAuthority::new)
                        .distinct()
                        .collect(Collectors.toList())
        );

        return result;
    }

    /**
     * 获取主键 id
     *
     * @return 主键 id
     */
    public Object getId() {
        return id;
    }

    /**
     * 设置主键 id
     *
     * @param id 主键 id
     */
    public void setId(Object id) {
        this.id = id;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取密码
     *
     * @return 密码
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * 设置状态:0.禁用,1启用
     *
     * @param status 状态:0.禁用,1启用
     */
    public void setStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * 获取状态:0.禁用,1启用
     *
     * @return 状态:0.禁用,1启用
     */
    public UserStatus getStatus() {
        return this.status;
    }

    /**
     * 设置登录帐号
     *
     * @param username 登录帐号
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取登录帐号
     *
     * @return 登录帐号
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return UserStatus.Enabled.equals(status);
    }

    /**
     * 获取資源类型的授权信息
     *
     * @return 授权信息集合
     */
    @JsonIgnore
    public List<ResourceAuthority> getResourceAuthorities() {
        return resourceAuthorities;
    }

    /**
     * 获取字符串集合的資源类型授权信息
     *
     * @return 字符串集合的資源类型授权信息
     */
    public List<String> getResourceAuthorityStrings() {
        return resourceAuthorities.stream().map(ResourceAuthority::getAuthority).collect(Collectors.toList());
    }

    /**
     * 设置資源类型的授权信息
     *
     * @param resourceAuthorities 資源类型的授权信息
     */
    public void setResourceAuthorities(List<ResourceAuthority> resourceAuthorities) {
        this.resourceAuthorities = resourceAuthorities;
    }

    /**
     * 获取角色类型的授权信息
     *
     * @return 角色类型的授权信息
     */
    public List<RoleAuthority> getRoleAuthorities() {
        return roleAuthorities;
    }

    /**
     * 设置角色类型的授权信息
     *
     * @param roleAuthorities 角色类型的授权信息
     */
    public void setRoleAuthorities(List<RoleAuthority> roleAuthorities) {
        this.roleAuthorities = roleAuthorities;
    }

    /**
     * 设置账户是否过期
     *
     * @param accountNonExpired true 是，否则 false
     */
    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    /**
     * 是否账户是否被锁定
     *
     * @param accountNonLocked true 是，否则 false
     */
    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    /**
     * 设置登陆凭证是否过期
     *
     * @param credentialsNonExpired true 是，否则 false
     */
    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置用户类型
     *
     * @param type 用户类型
     */
    public void setType(String type) {
        this.type = type;
    }
}
