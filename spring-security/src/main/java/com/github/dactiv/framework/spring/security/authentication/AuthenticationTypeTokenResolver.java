package com.github.dactiv.framework.spring.security.authentication;

import org.springframework.util.MultiValueMap;

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
     * 解码 token
     *
     * @param token toekn 值
     *
     * @return 参数信息
     */
    MultiValueMap<String, String> decode(String token);
}
