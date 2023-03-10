package com.github.dactiv.framework.spring.security.authentication.token;

import com.github.dactiv.framework.spring.security.authentication.rememberme.RememberMeToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 记住我认证 token
 *
 * @author maurice.chen
 */
public class RememberMeAuthenticationToken extends SimpleAuthenticationToken {

    private Object id;

    public RememberMeAuthenticationToken(String username, String type, Object id) {
        super(username, type, true);
    }

    public RememberMeAuthenticationToken(UsernamePasswordAuthenticationToken token, String type, Object id) {
        super(token, type, true);
    }

    public RememberMeAuthenticationToken(UsernamePasswordAuthenticationToken token, String type, UserDetails userDetails, Collection<? extends GrantedAuthority> authorities, Object id) {
        super(token, type, userDetails, authorities, true);
    }

    public RememberMeAuthenticationToken(UsernamePasswordAuthenticationToken token, String type, Object id, Collection<? extends GrantedAuthority> authorities) {
        super(token, type, true, authorities);
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public RememberMeToken toRememberToken() {
        RememberMeToken result = new RememberMeToken(getPrincipal().toString(), getCredentials().toString(), getType());
        result.setId(getId());
        return result;
    }

}
