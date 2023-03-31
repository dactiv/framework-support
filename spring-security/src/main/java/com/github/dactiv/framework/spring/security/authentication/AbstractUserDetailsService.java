package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * 抽象的用户明细服务实现，实现创建认证 token，等部分公用功能
 *
 * @author maurice.chen
 *
 * @param <T> 实体类型
 */
public abstract class AbstractUserDetailsService<T> implements UserDetailsService<T>{

    private final AuthenticationProperties properties;

    public AbstractUserDetailsService(AuthenticationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Authentication createToken(HttpServletRequest request, HttpServletResponse response, String type) {
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        username = StringUtils.defaultString(username, StringUtils.EMPTY).trim();
        password = StringUtils.defaultString(password, StringUtils.EMPTY);

        boolean rememberMe = obtainRememberMe(request);
        UsernamePasswordAuthenticationToken usernamePasswordToken = new UsernamePasswordAuthenticationToken(username, password);

        return new RequestAuthenticationToken(request, response, usernamePasswordToken, type, rememberMe);
    }

    /**
     * 获取记住我
     *
     * @param request http servlet request
     *
     * @return true 记住我，否则 false
     */
    protected boolean obtainRememberMe(HttpServletRequest request) {
        return BooleanUtils.toBoolean(request.getParameter(properties.getRememberMe().getParamName()));
    }

    /**
     * 获取登陆账户
     *
     * @param request http servlet request
     *
     * @return 登陆账户
     */
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(properties.getUsernameParamName());
    }

    /**
     * 获取登陆密码
     *
     * @param request http servlet request
     *
     * @return 登陆密码
     */
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(properties.getPasswordParamName());
    }
}
