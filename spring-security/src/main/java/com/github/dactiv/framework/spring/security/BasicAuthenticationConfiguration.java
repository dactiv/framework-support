package com.github.dactiv.framework.spring.security;

import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

/**
 * 基础认证 feign 配置
 *
 * @author maurice
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class BasicAuthenticationConfiguration {

    @Bean
    public RequestInterceptor basicAuthRequestInterceptor(SecurityProperties securityProperties) {
        return new BasicAuthRequestInterceptor(
                securityProperties.getUser().getName(),
                securityProperties.getUser().getPassword()
        );
    }

    /**
     * 创建 basic 认证的头信息
     *
     * @param username 认证账户
     * @param password 认证密码
     *
     * @return 头信息
     */
    public static HttpHeaders of(String username, String password) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String base64 = Base64.encodeBase64String((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        httpHeaders.add("Authorization", "Basic " + base64);
        return httpHeaders;
    }

    /**
     * 创建 basic 认证的头信息
     *
     * @param properties spring 安全配置
     *
     * @return 头信息
     */
    public static HttpHeaders of(SecurityProperties properties) {
        return of(properties.getUser().getName(),properties.getUser().getPassword());
    }

    /**
     * 创建带 basic 认证的 http 实体
     *
     * @param username 认证账户
     * @param password 认证密码
     *
     * @return 头信息
     */
    public static <T> HttpEntity<T> ofHttpEntity(T body, String username, String password) {
        return new HttpEntity<>(body, of(username, password));
    }

    /**
     * 创建带 basic 认证的 http 实体
     *
     * @param properties spring 安全配置
     *
     * @return 头信息
     */
    public static <T> HttpEntity<T> ofHttpEntity(T body, SecurityProperties properties) {
        return new HttpEntity<>(body, of(properties));
    }
}
