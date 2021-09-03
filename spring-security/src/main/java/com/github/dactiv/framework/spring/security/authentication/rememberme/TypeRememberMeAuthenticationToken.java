package com.github.dactiv.framework.spring.security.authentication.rememberme;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.springframework.security.authentication.RememberMeAuthenticationToken;

/**
 * 区分用户类型的记住我认证 token 实现
 *
 * @author maurice.chen
 */
public class TypeRememberMeAuthenticationToken extends RememberMeAuthenticationToken {

    private static final long serialVersionUID = -8113122218981886840L;

    /**
     * 类型
     *
     * @see com.github.dactiv.framework.spring.security.enumerate.ResourceSource
     */
    private final String type;

    public TypeRememberMeAuthenticationToken(SecurityUserDetails userDetails) {
        super(userDetails.getId().toString(), userDetails.getUsername(), userDetails.getAuthorities());
        setDetails(userDetails);
        this.type = userDetails.getType();
    }


    @Override
    public String getName() {
        return type + CacheProperties.DEFAULT_SEPARATOR + getPrincipal();
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public String getType() {
        return type;
    }
}
