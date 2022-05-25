package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.spring.security.audit.ControllerAuditHandlerInterceptor;
import com.github.dactiv.framework.spring.security.audit.RequestBodyAttributeAdviceAdapter;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
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
import com.github.dactiv.framework.spring.security.plugin.PluginSourceTypeVoter;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.websocket.servlet.UndertowWebSocketServletWebServerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
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
    @ConditionalOnMissingBean(DeviceIdContextRepository.class)
    public DeviceIdContextRepository deviceIdentifiedSecurityContextRepository(AuthenticationProperties properties,
                                                                        RedissonClient redissonClient) {

        DeviceIdContextRepository repository = new DeviceIdContextRepository(
                properties,
                redissonClient
        );

        repository.setAllowSessionCreation(properties.getDeviceId().isAllowSessionCreation());
        repository.setDisableUrlRewriting(properties.getDeviceId().isDisableUrlRewriting());

        return repository;
    }

    @Bean
    @ConditionalOnMissingBean(RememberMeServices.class)
    public CookieRememberService cookieRememberService(AuthenticationProperties properties,
                                                       RedissonClient redissonClient,
                                                       ObjectProvider<UserDetailsService> userDetailsService) {
        return new CookieRememberService(
                properties,
                redissonClient,
                userDetailsService.orderedStream().collect(Collectors.toList())
        );
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationSuccessHandler.class)
    public JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler(ObjectProvider<JsonAuthenticationFailureResponse> failureResponse) {
        return new JsonAuthenticationFailureHandler(failureResponse.orderedStream().collect(Collectors.toList()));
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
    public DefaultAuthenticationFailureResponse defaultAuthenticationFailureResponse(AuthenticationProperties properties) {
        return new DefaultAuthenticationFailureResponse(properties);
    }

    @Bean
    public FeignExceptionResultResolver feignExceptionResultResolver() {
        return new FeignExceptionResultResolver();
    }

    @Bean
    public FeignAuthenticationTypeTokenResolver feignAuthenticationTypeTokenResolver() {
        return new FeignAuthenticationTypeTokenResolver();
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<?>> decisionVoters
                = Arrays.asList(
                new WebExpressionVoter(),
                new RoleVoter(),
                new AuthenticatedVoter(),
                new PluginSourceTypeVoter()
        );

        ConsensusBased consensusBased = new ConsensusBased(decisionVoters);

        consensusBased.setAllowIfEqualGrantedDeniedDecisions(false);

        return consensusBased;
    }

    @Bean
    public RequestAuthenticationProvider requestAuthenticationProvider(RedissonClient redissonClient,
                                                                       ObjectProvider<UserDetailsService> userDetailsService) {
        return new RequestAuthenticationProvider(
                redissonClient,
                userDetailsService.orderedStream().collect(Collectors.toList())
        );
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
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
