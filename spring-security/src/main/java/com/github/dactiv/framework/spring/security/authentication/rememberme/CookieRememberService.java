package com.github.dactiv.framework.spring.security.authentication.rememberme;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * cookie 形式的记住我实现
 *
 * @author maurice.chen
 */
public class CookieRememberService implements RememberMeServices {

    private final static Logger LOGGER = LoggerFactory.getLogger(CookieRememberService.class);

    private final AuthenticationProperties properties;

    private final RedissonClient redissonClient;

    private final List<UserDetailsService> userDetailsServices;

    public CookieRememberService(AuthenticationProperties properties, RedissonClient redissonClient, List<UserDetailsService> userDetailsServices) {
        this.properties = properties;
        this.redissonClient = redissonClient;
        this.userDetailsServices = userDetailsServices;
    }

    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        String rememberMeCookie = getRememberMeCookieValue(request);

        if (StringUtils.isEmpty(rememberMeCookie)) {
            return null;
        }

        RememberMeToken rememberMeToken = getTokenValue(rememberMeCookie);

        if (Objects.isNull(rememberMeToken)) {
            return null;
        }

        RBucket<RememberMeToken> bucket = getRememberMeTokenBucket(rememberMeToken.getId());

        RememberMeToken redisObject = bucket.get();

        if (Objects.isNull(redisObject)) {
            removeCookie(request, response);
            return null;
        }

        // 如果两个 token 不相等，报错
        if (!rememberMeToken.equals(redisObject)) {
            removeCookie(request, response);
            return null;
        }

        UserDetailsService userDetailsService = getUserDetailsService(redisObject.getType());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                redisObject.getUsername(),
                redisObject.getToken()
        );
        PrincipalAuthenticationToken authenticationToken = new PrincipalAuthenticationToken(token, redisObject.getType());

        CacheProperties cache = userDetailsService.getAuthenticationCache(authenticationToken);
        String userDetailKey = cache.getName();

        // 先从获取认证缓存获取一次用户信息，如果没有。在调用获取认证用户明细方法，具体密码校验自身实现。
        RBucket<SecurityUserDetails> userDetailsBucket = redissonClient.getBucket(userDetailKey);
        SecurityUserDetails userDetails = userDetailsBucket.get();

        if (Objects.nonNull(userDetails)) {
            userDetailsBucket.expireAsync(cache.getExpiresTime().getValue(), cache.getExpiresTime().getUnit());
        } else {

            RequestAuthenticationToken requestToken = new RequestAuthenticationToken(
                    request,
                    response,
                    token,
                    redisObject.getType()
            );

            try {
                userDetails = userDetailsService.getAuthenticationUserDetails(requestToken);
                userDetails.setType(requestToken.getType());
            } catch (Exception e) {
                LOGGER.error("记住我服务授权出现错误", e);
                removeCookie(request, response);
                return null;
            }
        }

        TypeRememberMeAuthenticationToken result = new TypeRememberMeAuthenticationToken(userDetails);

        loginSuccess(request, response, result);

        return result;
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
        RBucket<RememberMeToken> bucket = getRememberMeTokenBucket(Casts.cast(details.getId(), Integer.class));

        RememberMeToken rememberMeToken = new RememberMeToken(details);
        bucket.setAsync(rememberMeToken);

        int maxAge = (int) properties.getRememberMe().getCache().getExpiresTime().toSeconds();

        removeCookie(request, response);
        setCookie(rememberMeToken, maxAge, request, response);
    }

    /**
     * 根据类型获取用户明细服务实现
     *
     * @param type 类型
     *
     * @return 用户明细服务实现
     */
    private UserDetailsService getUserDetailsService(String type) {
        return userDetailsServices
                .stream()
                .filter(u -> u.getType().contains(type))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + type + "] 的用户明细服务实现"));
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

        return optional.isEmpty() ? null : optional.get().getValue();
    }

    /**
     * 获取 redis 的记住我 token 桶信息
     *
     * @param id 主键 id
     *
     * @return 记住我 token 桶信息
     */
    public RBucket<RememberMeToken> getRememberMeTokenBucket(Integer id) {
        String key = properties.getRememberMe().getCache().getName(id);
        return redissonClient.getBucket(key);
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

        if (StringUtils.isEmpty(paramValue)) {
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

        if (maxAge < 1) {
            cookie.setVersion(1);
        }

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
                ? Base64.encodeBase64String(json.getBytes(StandardCharsets.UTF_8))
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
            token = new String(Base64.decodeBase64(token));
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
        return contextPath.length() > 0 ? contextPath : "/";
    }
}
