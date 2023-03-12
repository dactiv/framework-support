package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 认证 filter 实现, 用于结合 {@link UserDetailsService} 多用户类型认证的统一入口
 *
 * @author maurice.chen
 */
public class RequestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestAuthenticationFilter.class);
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final AuthenticationProperties authenticationProperties;

    private final List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers;

    private final List<UserDetailsService<?>> userDetailsServices;

    public RequestAuthenticationFilter(AuthenticationProperties authenticationProperties,
                                       List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolver,
                                       List<UserDetailsService<?>> userDetailsServices) {
        this.authenticationProperties = authenticationProperties;

        setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher(authenticationProperties.getLoginProcessingUrl(), HttpMethod.POST.name())
        );

        setUsernameParameter(authenticationProperties.getUsernameParamName());
        setPasswordParameter(authenticationProperties.getPasswordParamName());

        this.authenticationTypeTokenResolvers = authenticationTypeTokenResolver;
        this.userDetailsServices = userDetailsServices;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (this.securityContextHolderStrategy.getContext().getAuthentication() != null) {
            super.doFilter(request, response, chain);
            return;
        }

        Authentication token = getRememberMeServices().autoLogin(Casts.cast(request), Casts.cast(response));
        if (Objects.isNull(token)) {
            super.doFilter(request, response, chain);
            return;
        }

        try {
            Authentication authentication = getAuthenticationManager().authenticate(token);
            successfulAuthentication(Casts.cast(request), Casts.cast(response), chain, authentication);
            chain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            LOGGER.error("记住我认证出现异常", ex);
            unsuccessfulAuthentication(Casts.cast(request), Casts.cast(response), ex);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        super.successfulAuthentication(request, response, chain, authResult);
        String token = request.getHeader(authenticationProperties.getTokenHeaderName());

        if (StringUtils.isEmpty(StringUtils.trimToEmpty(token))) {
            return ;
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {

        if (StringUtils.isNotBlank(obtainType(request))) {
            return true;
        } else {
            return super.requiresAuthentication(request, response);
        }

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

        if (StringUtils.isBlank(type)) {
            throw new AuthenticationServiceException("授权类型不正确");
        }

        String token = request.getHeader(authenticationProperties.getTokenHeaderName());

        if (StringUtils.isNotBlank(token)) {
            String resolverType = request.getHeader(authenticationProperties.getTokenResolverHeaderName());

            AuthenticationTypeTokenResolver resolver = authenticationTypeTokenResolvers
                    .stream()
                    .filter(a -> a.isSupport(resolverType))
                    .findFirst()
                    .orElseThrow(() -> new SystemException("找不到类型 [" + resolverType + "] token 解析器实现"));

            return resolver.createToken(request, response, token);

        } else {
            UserDetailsService<?> userDetailsService = userDetailsServices
                    .stream()
                    .filter(u -> u.getType().contains(type))
                    .findFirst()
                    .orElseThrow(() -> new SystemException("找不到类型为 [" + type + "] 的用户明细服务实现"));

            return userDetailsService.createToken(request, response, type);
        }

    }

    /**
     * 获取类型
     *
     * @param request http servlet request
     *
     * @return 类型
     */
    protected String obtainType(HttpServletRequest request) {

        String type = request.getHeader(authenticationProperties.getTypeHeaderName());

        if (StringUtils.isBlank(type)) {
            type = request.getParameter(authenticationProperties.getTypeParamName());
        }

        return type;
    }


}
