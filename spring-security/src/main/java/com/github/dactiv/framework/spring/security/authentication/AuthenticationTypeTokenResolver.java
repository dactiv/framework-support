package com.github.dactiv.framework.spring.security.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证类型 token 解析器
 *
 * @author maurice.chen
 */
public interface AuthenticationTypeTokenResolver {

    /**
     * 是否支持
     *
     * @param type 类型名称
     *
     * @return true 是，否则 false
     */
    boolean isSupport(String type);

    /**
     * 创建认证类型 token
     *
     * @param request http servlet request
     * @param response http servlet response
     * @param token token 值
     *
     * @return 认证 token
     */
    Authentication createToken(HttpServletRequest request, HttpServletResponse response, String token);
}
