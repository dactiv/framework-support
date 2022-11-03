package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 账户认证的用户明细服务
 *
 * @author maurice.chen
 */
public interface UserDetailsService<T> {

    /**
     * 认证缓存块名称
     */
    String DEFAULT_AUTHENTICATION_KEY_NAME = "spring:security:authentication:";

    /**
     * 授权缓存块名称
     */
    String DEFAULT_AUTHORIZATION_KEY_NAME = "spring:security:authorization:";

    /**
     * 获取认证用户明细
     *
     * @param token 请求认证 token
     *
     * @return 用户明细
     *
     * @throws AuthenticationException 认证错误抛出的异常
     */
    SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token) throws AuthenticationException;

    /**
     * 获取用户授权集合
     *
     * @param userDetails 当前用户明细
     *
     * @return 用户授权集合
     */
    default Collection<? extends GrantedAuthority> getPrincipalAuthorities(SecurityUserDetails userDetails) {
        return userDetails.getAuthorities();
    }

    /**
     * 获取支持的用户类型
     *
     * @return 用户类型
     */
    List<String> getType();

    /**
     * 获取密码编码器
     *
     * @return 密码编码器
     */
    PasswordEncoder getPasswordEncoder();

    /**
     * 获取授权缓存
     *
     * @param token 当前用户认证 token
     *
     * @return 缓存名称
     */
    default CacheProperties getAuthorizationCache(PrincipalAuthenticationToken token) {
        return new CacheProperties(
                DEFAULT_AUTHORIZATION_KEY_NAME + token.getType() + ":" + token.getPrincipal(),
                new TimeProperties(7, TimeUnit.DAYS)
        );
    }

    /**
     * 获取授权缓存名称
     *
     * @param token 当前用户认证 token
     *
     * @return 缓存名称
     */
    default CacheProperties getAuthenticationCache(PrincipalAuthenticationToken token) {
        return new CacheProperties(
                DEFAULT_AUTHENTICATION_KEY_NAME + token.getType() + ":" + token.getPrincipal(),
                new TimeProperties(7, TimeUnit.DAYS)
        );
    }

    /**
     * 当认证成功的后置处理方法
     *
     * @param result 认证结果
     * @param request http servlet request
     * @param response http servlet response
     *
     * @throws AuthenticationException 处理成功认证流程出现异常后抛出
     */
    default void onSuccessAuthentication(PrincipalAuthenticationToken result, HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

    }

    /**
     * 匹配密码是否正确
     *
     * @param presentedPassword 提交过来的密码
     * @param token             请求认证 token
     * @param userDetails       spring security 用户实现
     *
     * @return true 是，否则 false
     */
    default boolean matchesPassword(String presentedPassword,
                                    RequestAuthenticationToken token,
                                    SecurityUserDetails userDetails) {
        return getPasswordEncoder().matches(presentedPassword, userDetails.getPassword());
    }

    /**
     * 转换目标用户
     *
     * @param userDetails 当前安全用户明细
     *
     * @return 目标用户实体
     */
    default T convertTargetUser(SecurityUserDetails userDetails) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    /**
     * 转换目标用户
     *
     * @param userDetails 用户明细
     *
     * @return 目标用户实体
     */
    default T convertTargetUser(TypeUserDetails<?> userDetails) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    /**
     * 更新目标用户密码
     *
     * @param targetUser 目标用户
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    default void updatePassword(T targetUser , String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    /**
     * 创建认证 token
     *
     * @param request http servlet request
     * @param response http servlet response
     * @param type 认证类型
     *
     * @return 认证 token
     */
    Authentication createToken(HttpServletRequest request, HttpServletResponse response, String type);

    /**
     * 创建认证成功 token
     *
     * @param userDetails 当亲啊用户
     * @param token token 信息
     * @param grantedAuthorities 权限信息
     *
     * @return 新的认证 token
     */
    default PrincipalAuthenticationToken createSuccessAuthentication(SecurityUserDetails userDetails, PrincipalAuthenticationToken token, Collection<? extends GrantedAuthority> grantedAuthorities) {
        return new PrincipalAuthenticationToken(
                new UsernamePasswordAuthenticationToken(token.getPrincipal(), token.getCredentials()),
                token.getType(),
                userDetails,
                grantedAuthorities,
                false
        );
    }
}
