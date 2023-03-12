package com.github.dactiv.framework.spring.security.authentication.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;

/**
 * 认证成功后的当前用户 token
 *
 * @author maurice.chen
 */
public class PrincipalAuthenticationToken extends SimpleAuthenticationToken {

    /**
     * 最后认证时间
     */
    private Date lastAuthenticationTime;

    public PrincipalAuthenticationToken(String username, String type, boolean rememberMe, Date lastAuthenticationTime) {
        super(username, type, rememberMe);
        this.lastAuthenticationTime = lastAuthenticationTime;
    }

    public PrincipalAuthenticationToken(UsernamePasswordAuthenticationToken token, String type, boolean rememberMe, Date lastAuthenticationTime) {
        super(token, type, rememberMe);
        this.lastAuthenticationTime = lastAuthenticationTime;
    }

    public PrincipalAuthenticationToken(UsernamePasswordAuthenticationToken token, String type, UserDetails userDetails, Collection<? extends GrantedAuthority> authorities, boolean rememberMe, Date lastAuthenticationTime) {
        super(token, type, userDetails, authorities, rememberMe);
        this.lastAuthenticationTime = lastAuthenticationTime;
    }

    public PrincipalAuthenticationToken(UsernamePasswordAuthenticationToken token, String type, UserDetails userDetails, boolean rememberMe, Date lastAuthenticationTime) {
        super(token, type, userDetails, userDetails.getAuthorities(), rememberMe);
        this.lastAuthenticationTime = lastAuthenticationTime;
    }

    public PrincipalAuthenticationToken(UsernamePasswordAuthenticationToken token, String type, boolean rememberMe, Collection<? extends GrantedAuthority> authorities, Date lastAuthenticationTime) {
        super(token, type, rememberMe, authorities);
        this.lastAuthenticationTime = lastAuthenticationTime;
    }

    /**
     * 获取最后认证时间
     *
     * @return 最后认证时间
     */
    public Date getLastAuthenticationTime() {
        return lastAuthenticationTime;
    }

    /**
     * 设置最后认证时间
     *
     * @param lastAuthenticationTime 最后认证时间
     */
    public void setLastAuthenticationTime(Date lastAuthenticationTime) {
        this.lastAuthenticationTime = lastAuthenticationTime;
    }
}
