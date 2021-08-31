package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.spring.security.audit.ControllerAuditHandlerInterceptor;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureResponse;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessResponse;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultAuthenticationFailureResponse;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultUserDetailsService;
import com.github.dactiv.framework.spring.security.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.spring.security.concurrent.ConcurrentPointcutAdvisor;
import com.github.dactiv.framework.spring.security.concurrent.key.KeyGenerator;
import com.github.dactiv.framework.spring.security.concurrent.key.support.SpelExpressionKeyGenerator;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import com.github.dactiv.framework.spring.security.plugin.PluginSourceTypeVoter;
import com.github.dactiv.framework.spring.security.version.AccessVersionControlHandlerInterceptor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * spring security 重写支持自动配置类
 *
 * @author maurice
 */
@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableConfigurationProperties(AuthenticationProperties.class)
@ConditionalOnProperty(prefix = "authentication.spring.security", value = "enabled", matchIfMissing = true)
public class SpringSecuritySupportAutoConfiguration {

    @Autowired(required = false)
    private List<InfoContributor> infoContributors;

    @Bean
    @ConfigurationProperties("authentication.plugin")
    PluginEndpoint pluginEndpoint() {
        return new PluginEndpoint(infoContributors);
    }

    @Bean
    ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor() {
        return new ControllerAuditHandlerInterceptor();
    }

    @Bean
    AccessVersionControlHandlerInterceptor accessVersionControlHandlerInterceptor() {
        return new AccessVersionControlHandlerInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(KeyGenerator.class)
    KeyGenerator keyGenerator() {
        return new SpelExpressionKeyGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(ConcurrentPointcutAdvisor.class)
    ConcurrentPointcutAdvisor concurrentPointcutAdvisor(RedissonClient redissonClient, KeyGenerator keyGenerator) {
        return new ConcurrentPointcutAdvisor(new ConcurrentInterceptor(redissonClient, keyGenerator));
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationSuccessHandler.class)
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DefaultUserDetailsService defaultUserDetailsService(PasswordEncoder passwordEncoder,
                                                        AuthenticationProperties properties,
                                                        RedissonClient redissonClient) {

        return new DefaultUserDetailsService(properties, passwordEncoder, redissonClient);
    }

    @Bean
    DeviceIdContextRepository deviceIdentifiedSecurityContextRepository(AuthenticationProperties properties,
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
    @ConditionalOnMissingBean(JsonAuthenticationSuccessHandler.class)
    public JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler(List<JsonAuthenticationFailureResponse> failureResponses) {
        return new JsonAuthenticationFailureHandler(failureResponses);
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationSuccessHandler.class)
    public JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler(List<JsonAuthenticationSuccessResponse> successResponses,
                                                                             AuthenticationProperties properties) {
        return new JsonAuthenticationSuccessHandler(successResponses, properties);
    }

    @Bean
    public DefaultAuthenticationFailureResponse defaultAuthenticationFailureResponse(AuthenticationProperties properties) {
        return new DefaultAuthenticationFailureResponse(properties);
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<? extends Object>> decisionVoters
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
                                                                       List<UserDetailsService> userDetailsServices) {
        return new RequestAuthenticationProvider(redissonClient, userDetailsServices);
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class DefaultWebMvcConfigurer implements WebMvcConfigurer {

        @Autowired
        private ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor;

        @Autowired
        private AccessVersionControlHandlerInterceptor accessVersionControlHandlerInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(controllerAuditHandlerInterceptor);
            registry.addInterceptor(accessVersionControlHandlerInterceptor);
        }
    }
}
