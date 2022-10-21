package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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
import org.springframework.util.MultiValueMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证 filter 实现, 用于结合 {@link UserDetailsService} 多用户类型认证的统一入口
 *
 * @author maurice.chen
 */
public class RequestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationProperties properties;

    private final List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            Authentication rememberMeAuth = getRememberMeServices().autoLogin(request, response);

            if (rememberMeAuth != null) {
                successfulAuthentication(request, response, chain, rememberMeAuth);
                return;
            }
        }

        super.doFilter(req, res, chain);
    }

    public RequestAuthenticationFilter(AuthenticationProperties properties,
                                       List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolver) {
        this.properties = properties;

        setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher(properties.getLoginProcessingUrl(), HttpMethod.POST.name())
        );

        setUsernameParameter(properties.getUsernameParamName());
        setPasswordParameter(properties.getPasswordParamName());

        this.authenticationTypeTokenResolvers = authenticationTypeTokenResolver;
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

        if (StringUtils.isBlank(type)) {
            throw new AuthenticationServiceException("授权类型不正确");
        }

        String token = request.getHeader(properties.getTokenHeaderName());
        String username;
        String password;
        boolean rememberMe;

        if (StringUtils.isNotBlank(token)) {
            String resolverType = request.getHeader(properties.getTokenResolverHeaderName());

            List<AuthenticationTypeTokenResolver> resolvers = authenticationTypeTokenResolvers
                    .stream()
                    .filter(a -> a.isSupport(resolverType))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(resolvers)) {
                throw new SystemException("找不到类型 [" + resolverType + "] token 解析器实现");
            }

            if (resolvers.size() > 1) {
                throw new SystemException("针对 [" + resolverType + "] 类型找到一个以上的 token 解析器实现");
            }

            MultiValueMap<String, String> body = resolvers.iterator().next().decode(token);

            username = body.getFirst(properties.getUsernameParamName());
            password = body.getFirst(properties.getPasswordParamName());
            rememberMe = BooleanUtils.toBoolean(body.getFirst(properties.getRememberMe().getParamName()));
        } else {
            username = obtainUsername(request);
            password = obtainPassword(request);

            username = StringUtils.defaultString(username, StringUtils.EMPTY).trim();
            password = StringUtils.defaultString(password, StringUtils.EMPTY);

            rememberMe = obtainRememberMe(request);
        }

        UsernamePasswordAuthenticationToken usernamePasswordToken = new UsernamePasswordAuthenticationToken(username, password);

        if (request.getRequestURI().equals(properties.getLoginProcessingUrl())) {
            rememberMe = false;
        }

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
     * 获取类型
     *
     * @param request http servlet request
     *
     * @return 类型
     */
    protected String obtainType(HttpServletRequest request) {

        String type = request.getHeader(properties.getTypeHeaderName());

        if (StringUtils.isBlank(type)) {
            type = request.getParameter(properties.getTypeParamName());
        }

        return type;
    }


}
