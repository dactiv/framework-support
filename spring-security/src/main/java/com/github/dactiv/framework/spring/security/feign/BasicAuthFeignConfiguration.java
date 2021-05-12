package com.github.dactiv.framework.spring.security.feign;

import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基础认证 feign 配置
 *
 * @author maurice
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class BasicAuthFeignConfiguration {

    @Bean
    public RequestInterceptor basicAuthRequestInterceptor(SecurityProperties securityProperties) {
        return new BasicAuthRequestInterceptor(
                securityProperties.getUser().getName(),
                securityProperties.getUser().getPassword()
        );
    }
}
