package com.github.dactiv.framework.spring.security.authentication.rememberme;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.RememberMeAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * cookie 形式的记住我实现
 *
 * @author maurice.chen
 */
public class CookieRememberService implements RememberMeServices {

    private final AuthenticationProperties properties;

    private final RedissonClient redissonClient;

    public CookieRememberService(AuthenticationProperties properties, RedissonClient redissonClient) {
        this.properties = properties;
        this.redissonClient = redissonClient;
    }

    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        String rememberMeCookie = getRememberMeCookieValue(request);

        if (StringUtils.isBlank(rememberMeCookie)) {
            return null;
        }

        RememberMeToken rememberMeToken = getTokenValue(rememberMeCookie);

        if (Objects.isNull(rememberMeToken)) {
            return null;
        }

        return new RememberMeAuthenticationToken(
                new UsernamePasswordAuthenticationToken(rememberMeToken.getUsername(), rememberMeToken.getToken()),
                rememberMeToken.getType(),
                rememberMeToken.getId()
        );
    }

    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {
        removeCookie(request, response);
    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        if (!isRememberMeRequested(request)) {
            return;
        }

        if (!SecurityUserDetails.class.isAssignableFrom(authentication.getDetails().getClass())) {
            return;
        }

        SecurityUserDetails details = Casts.cast(authentication.getDetails());

        String key = properties.getRememberMe().getCache().getName(Casts.cast(details.getId(), Integer.class));
        RBucket<RememberMeToken> bucket = redissonClient.getBucket(key);

        RememberMeToken rememberMeToken = new RememberMeToken(details);
        bucket.setAsync(rememberMeToken);

        if (Objects.nonNull(properties.getRememberMe().getCache().getExpiresTime())) {
            rememberMeToken = bucket.get();
            bucket.expireAsync(properties.getRememberMe().getCache().getExpiresTime().toDuration());
        }

        int maxAge = (int) properties.getRememberMe().getCache().getExpiresTime().toSeconds();

        //removeCookie(request, response);
        setCookie(rememberMeToken, maxAge, request, response);
    }

    /**
     * 获取记住我的 cookie 值
     *
     * @param request http 请求
     *
     * @return 记住我的 cookie 值
     */
    protected String getRememberMeCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (ArrayUtils.isEmpty(cookies)) {
            return null;
        }

        Optional<Cookie> optional = Arrays
                .stream(cookies)
                .filter(c -> c.getName().equals(properties.getRememberMe().getCookie().getName()))
                .findFirst();

        return optional.map(Cookie::getValue).orElse(null);
    }

    /**
     * 是否记住我请求
     *
     * @param request http 请求
     *
     * @return true 是，否则 false
     */
    protected boolean isRememberMeRequested(HttpServletRequest request) {
        // 如果配置永远使用记住我，永远返回 true
        if (properties.getRememberMe().isAlways()) {
            return true;
        }

        String paramValue = request.getParameter(properties.getRememberMe().getParamName());

        if (StringUtils.isBlank(paramValue)) {
            return false;
        }

        return BooleanUtils.toBoolean(paramValue);
    }

    /**
     * 设置 cookie 内容
     *
     * @param token    token 信息
     * @param maxAge   过期时间
     * @param request  http 请求信息
     * @param response http 请求信息
     */
    protected void setCookie(RememberMeToken token, int maxAge, HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = encodeTokenValue(token);

        Cookie cookie = createCookie(request);

        cookie.setMaxAge(maxAge);
        cookie.setValue(cookieValue);

        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }

    /**
     * 编码 token 值
     *
     * @param token token 信息
     *
     * @return 编码后的 token 值
     */
    private String encodeTokenValue(RememberMeToken token) {

        String json = Casts.writeValueAsString(token);

        return properties.getRememberMe().isBase64Value()
                ? Base64.encodeToString(json.getBytes(StandardCharsets.UTF_8))
                : json;
    }

    /**
     * 获取获取 token 值，并转换为对象
     *
     * @param token token 值
     *
     * @return 记住我 token
     */
    private RememberMeToken getTokenValue(String token) {

        if (properties.getRememberMe().isBase64Value()) {
            token = Base64.decodeToString(token);
        }

        return Casts.readValue(token, RememberMeToken.class);
    }

    /**
     * 创建一个新的 cookie
     *
     * @param request http 请求信息
     *
     * @return cookie
     */
    protected Cookie createCookie(HttpServletRequest request) {
        Cookie cookie = new Cookie(properties.getRememberMe().getCookie().getName(), null);
        cookie.setPath(getCookiePath(request));

        if (StringUtils.isNotEmpty(properties.getRememberMe().getCookie().getDomain())) {
            cookie.setDomain(properties.getRememberMe().getCookie().getDomain());
        }

        if (properties.getRememberMe().getCookie().getSecure() == null) {
            cookie.setSecure(request.isSecure());
        } else {
            cookie.setSecure(properties.getRememberMe().getCookie().getSecure());
        }

        return cookie;
    }

    /**
     * 删除 cookie
     *
     * @param request  http 请求信息
     * @param response http 响应信息
     */
    protected void removeCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = createCookie(request);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * 获取 cookie 路径
     *
     * @param request http 请求信息
     *
     * @return cookie 路径
     */
    private String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : AntPathMatcher.DEFAULT_PATH_SEPARATOR;
    }
}
