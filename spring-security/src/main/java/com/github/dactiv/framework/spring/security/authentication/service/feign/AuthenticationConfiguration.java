package com.github.dactiv.framework.spring.security.authentication.service.feign;

import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultUserDetailsService;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * 基础认证 feign 配置
 *
 * @author maurice
 */
@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
public class AuthenticationConfiguration {

    public static final String DEFAULT_USERNAME = "feign";

    @Bean
    public RequestInterceptor basicAuthRequestInterceptor(AuthenticationProperties properties) {
        return requestTemplate -> {

            Optional<SecurityProperties.User> optional = properties
                    .getUsers()
                    .stream()
                    .flatMap(u -> u.getData().stream())
                    .filter(u -> u.getName().equals(DEFAULT_USERNAME))
                    .findFirst();

            if (optional.isEmpty()) {
                return ;
            }

            SecurityProperties.User user = optional.get();

            requestTemplate.method(Request.HttpMethod.POST);

            requestTemplate.header(properties.getTypeHeaderName(), DefaultUserDetailsService.DEFAULT_TYPES);

            requestTemplate.query(properties.getUsernameParamName(), user.getName());
            requestTemplate.query(properties.getPasswordParamName(), user.getPassword());
        };
    }
}
