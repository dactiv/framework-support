package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.spring.security.audit.ControllerAuditHandlerInterceptor;
import com.github.dactiv.framework.spring.security.audit.RequestBodyAttributeAdviceAdapter;
import com.github.dactiv.framework.spring.security.authentication.AccessTokenContextRepository;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureResponse;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessResponse;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultAuthenticationFailureResponse;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultUserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationTypeTokenResolver;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignExceptionResultResolver;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.websocket.servlet.UndertowWebSocketServletWebServerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.stream.Collectors;

/**
 * spring security 重写支持自动配置类
 *
 * @author maurice
 */
@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableConfigurationProperties(AuthenticationProperties.class)
@ConditionalOnProperty(prefix = "dactiv.authentication.spring.security", value = "enabled", matchIfMissing = true)
public class SpringSecurityAutoConfiguration {

    @Bean
    @ConfigurationProperties("dactiv.authentication.plugin")
    PluginEndpoint pluginEndpoint(ObjectProvider<InfoContributor> infoContributor) {
        return new PluginEndpoint(infoContributor.stream().collect(Collectors.toList()));
    }

    @Bean
    @ConditionalOnProperty(prefix = "dactiv.authentication.audit", name = "enabled", havingValue = "true")
    ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor() {
        return new ControllerAuditHandlerInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationSuccessHandler.class)
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DefaultUserDetailsService defaultUserDetailsService(PasswordEncoder passwordEncoder,
                                                        AuthenticationProperties properties) {

        return new DefaultUserDetailsService(properties, passwordEncoder);
    }

    @Bean
    @ConditionalOnMissingBean(AccessTokenContextRepository.class)
    public AccessTokenContextRepository accessTokenContextRepository(AuthenticationProperties properties,
                                                                     RedissonClient redissonClient) {

        return new AccessTokenContextRepository(
                redissonClient,
                properties
        );
    }

    @Bean
    @ConditionalOnMissingBean(RememberMeServices.class)
    public CookieRememberService cookieRememberService(AuthenticationProperties properties, RedissonClient redissonClient) {
        return new CookieRememberService(properties, redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationSuccessHandler.class)
    public JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler(ObjectProvider<JsonAuthenticationFailureResponse> failureResponse,
                                                                             AuthenticationProperties authenticationProperties) {
        return new JsonAuthenticationFailureHandler(failureResponse.orderedStream().collect(Collectors.toList()), authenticationProperties);
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationSuccessHandler.class)
    public JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler(ObjectProvider<JsonAuthenticationSuccessResponse> successResponse,
                                                                             AuthenticationProperties properties) {
        return new JsonAuthenticationSuccessHandler(
                successResponse.orderedStream().collect(Collectors.toList()),
                properties
        );
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager(RedissonClient redissonClient,
                                                       AuthenticationProperties authenticationProperties,
                                                       ObjectProvider<UserDetailsService> userDetailsService) {
        return new RequestAuthenticationProvider(
                redissonClient,
                authenticationProperties,
                userDetailsService.orderedStream().collect(Collectors.toList())
        );
    }

    @Bean
    public DefaultAuthenticationFailureResponse defaultAuthenticationFailureResponse(AuthenticationProperties properties) {
        return new DefaultAuthenticationFailureResponse(properties);
    }

    @Bean
    @ConditionalOnClass(feign.FeignException.class)
    public FeignExceptionResultResolver feignExceptionResultResolver() {
        return new FeignExceptionResultResolver();
    }

    @Bean
    public FeignAuthenticationTypeTokenResolver feignAuthenticationTypeTokenResolver(AuthenticationProperties properties) {
        return new FeignAuthenticationTypeTokenResolver(properties);
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "dactiv.authentication.audit", name = "enabled", havingValue = "true")
    public static class DefaultWebMvcConfigurer extends UndertowWebSocketServletWebServerCustomizer implements WebMvcConfigurer {

        private final ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor;

        public DefaultWebMvcConfigurer(ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor) {
            this.controllerAuditHandlerInterceptor = controllerAuditHandlerInterceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(controllerAuditHandlerInterceptor);
        }
    }

    @Bean
    @ConditionalOnMissingBean(RequestBodyAttributeAdviceAdapter.class)
    public RequestBodyAttributeAdviceAdapter requestBodyAttributeAdviceAdapter() {
        return new RequestBodyAttributeAdviceAdapter();
    }
}
