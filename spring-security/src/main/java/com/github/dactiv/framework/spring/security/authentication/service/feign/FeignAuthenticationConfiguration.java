package com.github.dactiv.framework.spring.security.authentication.service.feign;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultUserDetailsService;
import feign.RequestInterceptor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

/**
 * 基础认证 feign 配置
 *
 * @author maurice
 */
@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
public class FeignAuthenticationConfiguration {

    /**
     * feign 调用认真拦截器
     *
     * @param properties 认真配置信息
     *
     * @return feign 请求拦截器
     */
    @Bean
    public RequestInterceptor feignAuthRequestInterceptor(AuthenticationProperties properties) {
        return requestTemplate -> {

            SecurityProperties.User user = properties
                    .getUsers()
                    .stream()
                    .filter(u -> u.getName().equals(FeignAuthenticationTypeTokenResolver.DEFAULT_TYPE))
                    .findFirst()
                    .orElseThrow(() -> new SystemException("找不到类型为:" + FeignAuthenticationTypeTokenResolver.DEFAULT_TYPE + "的默认用户"));

            requestTemplate.header(properties.getTypeHeaderName(), DefaultUserDetailsService.DEFAULT_TYPES);

            String base64 = encodeUserProperties(properties, user);

            requestTemplate.header(properties.getTokenHeaderName(), base64);
            requestTemplate.header(properties.getTokenResolverHeaderName(), FeignAuthenticationTypeTokenResolver.DEFAULT_TYPE);
        };
    }

    /**
     * 对认证用户进行编码
     *
     * @param properties 认证配置
     * @param user 当前用户
     *
     * @return 编码后的字符串
     */
    public static String encodeUserProperties(AuthenticationProperties properties, SecurityProperties.User user) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        requestBody.add(properties.getUsernameParamName(), user.getName());
        requestBody.add(properties.getPasswordParamName(), user.getPassword());

        String token = Casts.castRequestBodyMapToString(requestBody);

        return Base64.encodeBase64String(token.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 构造 feign 认证的 http headers
     *
     * @param properties 认证配置信息
     *
     * @return feign 认证的 http headers
     */
    public static HttpHeaders of(AuthenticationProperties properties) {
        HttpHeaders httpHeaders = new HttpHeaders();

        SecurityProperties.User user = properties
                .getUsers()
                .stream()
                .filter(u -> u.getName().equals(FeignAuthenticationTypeTokenResolver.DEFAULT_TYPE))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为:" + FeignAuthenticationTypeTokenResolver.DEFAULT_TYPE + "的默认用户"));

        String base64 = encodeUserProperties(properties, user);

        httpHeaders.add(properties.getTypeHeaderName(), DefaultUserDetailsService.DEFAULT_TYPES);
        httpHeaders.add(properties.getTokenHeaderName(), base64);
        httpHeaders.add(properties.getTokenResolverHeaderName(), FeignAuthenticationTypeTokenResolver.DEFAULT_TYPE);

        return httpHeaders;
    }

}
