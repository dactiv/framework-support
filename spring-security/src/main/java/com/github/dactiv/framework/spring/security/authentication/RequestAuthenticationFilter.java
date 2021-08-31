package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证 filter 实现, 用于结合 {@link UserDetailsService} 多用户类型认证的统一入口
 *
 * @author maurice.chen
 */
public class RequestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationProperties properties;

    public RequestAuthenticationFilter(AuthenticationProperties properties) {
        this.properties = properties;

        setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher(properties.getLoginProcessingUrl(), HttpMethod.POST.name())
        );

        setUsernameParameter(properties.getUsernameParamName());
        setPasswordParameter(properties.getPasswordParamName());
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return super.requiresAuthentication(request, response) || StringUtils.isNotEmpty(obtainType(request));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        if (!HttpMethod.POST.matches(request.getMethod()) && super.requiresAuthentication(request, response)) {
            throw new AuthenticationServiceException("不支持 [" + request.getMethod() + "] 方式的登陆请求");
        }

        Authentication token = createToken(request, response);

        return getAuthenticationManager().authenticate(token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        SecurityContextHolder.getContext().setAuthentication(authResult);

        getRememberMeServices().loginSuccess(request, response, authResult);

        // 推送用户成功登陆事件
        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }

        getSuccessHandler().onAuthenticationSuccess(request, response, chain, authResult);
    }

    /**
     * 创建当前用户认证 token
     *
     * @param request  http servlet request
     * @param response http servlet response
     *
     * @return 当前用户认证 token
     *
     * @throws AuthenticationException 认证异常
     */
    public Authentication createToken(HttpServletRequest request,
                                      HttpServletResponse response) throws AuthenticationException {

        String type = obtainType(request);

        if (StringUtils.isEmpty(type)) {
            throw new AuthenticationServiceException("授权类型不正确");
        }

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

        return new RequestAuthenticationToken(request, response, token, type);

    }

    /**
     * 获取类型
     *
     * @param request http servlet request
     *
     * @return 类型
     */
    protected String obtainType(HttpServletRequest request) {

        String type =  request.getHeader(properties.getTypeHeaderName());

        if (StringUtils.isEmpty(type)) {
            type = request.getParameter(properties.getTypeParamName());
        }

        return type;
    }


}
