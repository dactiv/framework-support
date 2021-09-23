package com.github.dactiv.framework.spring.security.authentication.service.feign;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.authentication.AuthenticationTypeTokenResolver;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

/**
 * feign 认证类型 token 解析器实现
 *
 * @author maurice.chen
 */
public class FeignAuthenticationTypeTokenResolver implements AuthenticationTypeTokenResolver {

    public static final String DEFAULT_TYPE = "feign";

    @Override
    public boolean isSupport(String type) {
        return DEFAULT_TYPE.equals(type);
    }

    @Override
    public MultiValueMap<String, String> decode(String token) {
        return decodeUserProperties(token);
    }

    /**
     * 解码用户配置
     *
     * @param token token 值
     *
     * @return 参数信息
     */
    public static MultiValueMap<String, String> decodeUserProperties(String token) {
        String param = new String(Base64.decodeBase64(token), StandardCharsets.UTF_8);
        return Casts.castRequestBodyMap(param);
    }
}
