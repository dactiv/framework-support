package com.github.dactiv.framework.spring.security.authentication.service.feign;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * feign 认证类型 token 解析器实现
 *
 * @author maurice.chen
 */
public class FeignAuthenticationTypeTokenResolver implements AuthenticationTypeTokenResolver {

    public static final String DEFAULT_TYPE = "feign";

    private final AuthenticationProperties properties;

    public FeignAuthenticationTypeTokenResolver(AuthenticationProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isSupport(String type) {
        return DEFAULT_TYPE.equals(type);
    }

    @Override
    public Authentication createToken(HttpServletRequest request, HttpServletResponse response, String token) {
        MultiValueMap<String, String> body = decodeUserProperties(token);
        String username = body.getFirst(properties.getUsernameParamName());
        String password = body.getFirst(properties.getPasswordParamName());
        return new UsernamePasswordAuthenticationToken(username, password);
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
