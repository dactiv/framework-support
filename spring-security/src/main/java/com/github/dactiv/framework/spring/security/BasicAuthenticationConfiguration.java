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
     * @return 头信息
     */
    public static HttpHeaders createBasicAuthHttpHeaders(String username, String password) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String base64 = Base64.encodeBase64String((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        httpHeaders.add("Authorization", "Basic " + base64);
        return httpHeaders;
    }
}