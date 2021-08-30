package com.github.dactiv.framework.spring.security.authentication.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 当前用户认证 token
 *
 * @author maurice.chen
 */
public class PrincipalAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3747271533448473641L;

    private final UsernamePasswordAuthenticationToken token;

    /**
     * 用户类型
     */
    private final String type;

    /**
     * 当前用户认证 token
     *
     * @param token 登陆账户密码认证令牌
     * @param type  用户类型
     */
    public PrincipalAuthenticationToken(UsernamePasswordAuthenticationToken token,
                                        String type) {
        this(token, type, new ArrayList<>());
    }

    /**
     * 当前用户认证 token
     *
     * @param token       登陆账户密码认证令牌
     * @param type        用户类型
     * @param userDetails 用户明细实体
     * @param authorities 授权信息
     */
    public PrincipalAuthenticationToken(UsernamePasswordAuthenticationToken token,
                                        String type,
                                        UserDetails userDetails,
                                        Collection<? extends GrantedAuthority> authorities) {

        this(token, type, authorities);
        setAuthenticated(true);
        setDetails(userDetails);
    }

    /**
     * 当前用户认证 token
     *
     * @param token       登陆账户密码认证令牌
     * @param type        用户类型
     * @param authorities 授权信息
     */
    public PrincipalAuthenticationToken(UsernamePasswordAuthenticationToken token,
                                        String type,
                                        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.type = type;
    }

    @Override
    public Object getCredentials() {
        return token.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return token.getPrincipal();
    }

    @Override
    public String getName() {
        return getType() + ":" + token.getPrincipal();
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public String getType() {
        return type;
    }
}
