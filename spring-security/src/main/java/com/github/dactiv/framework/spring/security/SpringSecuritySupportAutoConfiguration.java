package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.spring.security.asscess.UserTypeVoter;
import com.github.dactiv.framework.spring.security.audit.ControllerAuditHandlerInterceptor;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdentifiedSecurityContextRepository;
import com.github.dactiv.framework.spring.security.authentication.provider.AnonymousUserAuthenticationProvider;
import com.github.dactiv.framework.spring.security.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.spring.security.concurrent.ConcurrentPointcutAdvisor;
import com.github.dactiv.framework.spring.security.concurrent.key.KeyGenerator;
import com.github.dactiv.framework.spring.security.concurrent.key.support.SpelExpressionKeyGenerator;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import com.github.dactiv.framework.spring.security.entity.RoleAuthority;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import com.github.dactiv.framework.spring.security.version.AccessVersionControlHandlerInterceptor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * spring security 重写支持自动配置类
 *
 * @author maurice
 */
@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableConfigurationProperties({SpringSecuritySupportProperties.class, SecurityProperties.class})
@ConditionalOnProperty(prefix = "spring.security.support", value = "enabled", matchIfMissing = true)
public class SpringSecuritySupportAutoConfiguration {

    @Autowired(required = false)
    private List<InfoContributor> infoContributors;

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

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "spring.security.support", value = "override-http-security", matchIfMissing = true)
    static class DefaultConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private DeviceIdentifiedSecurityContextRepository deviceIdentifiedSecurityContextRepository;

        @Autowired
        private AnonymousUserAuthenticationProvider anonymousUserAuthenticationProvider;

        @Autowired
        private DaoAuthenticationProvider daoAuthenticationProvider;

        @Override
        protected void configure(AuthenticationManagerBuilder managerBuilder) {

            managerBuilder
                    .authenticationProvider(daoAuthenticationProvider)
                    .authenticationProvider(anonymousUserAuthenticationProvider);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.authorizeRequests()
                    .anyRequest()
                    .permitAll()
                    .and()
                    .formLogin()
                    .disable()
                    .logout()
                    .disable()
                    .httpBasic()
                    .and()
                    .cors()
                    .disable()
                    .csrf()
                    .disable()
                    .requestCache()
                    .disable()
                    .securityContext()
                    .securityContextRepository(deviceIdentifiedSecurityContextRepository);

            addConsensusBasedToMethodSecurityInterceptor(http);

        }

    }

    /**
     * 添加 ConsensusBased 访问管理器到方法拦截器中
     *
     * @param http http security
     */
    public static void addConsensusBasedToMethodSecurityInterceptor(HttpSecurity http) {
        try {
            MethodSecurityInterceptor methodSecurityInterceptor = http
                    .getSharedObject(ApplicationContext.class)
                    .getBean(MethodSecurityInterceptor.class);

            AccessDecisionManager accessDecisionManager = methodSecurityInterceptor.getAccessDecisionManager();

            if (AbstractAccessDecisionManager.class.isAssignableFrom(accessDecisionManager.getClass())) {

                AbstractAccessDecisionManager adm = (AbstractAccessDecisionManager) accessDecisionManager;
                adm.getDecisionVoters().add(new UserTypeVoter());

                ConsensusBased consensusBased = new ConsensusBased(adm.getDecisionVoters());
                consensusBased.setAllowIfEqualGrantedDeniedDecisions(false);

                methodSecurityInterceptor.setAccessDecisionManager(consensusBased);
            }
        } catch (Exception ignored) {

        }
    }

    @Bean
    UserTypeVoter userTypeVoter() {
        return new UserTypeVoter();
    }

    @Bean
    @ConfigurationProperties("plugin")
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
    DeviceIdentifiedSecurityContextRepository deviceIdentifiedSecurityContextRepository(RedissonClient redissonClient,
                                                                                        SpringSecuritySupportProperties properties) {

        DeviceIdentifiedSecurityContextRepository repository = new DeviceIdentifiedSecurityContextRepository(
                properties.getCache(),
                redissonClient,
                properties.getLoginProcessingUrl()
        );

        repository.setAllowSessionCreation(properties.getAllowSessionCreation());
        repository.setDisableUrlRewriting(properties.getDisableUrlRewriting());

        return repository;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager(SecurityProperties securityProperties,
                                                          PasswordEncoder passwordEncoder) {

        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

        SecurityProperties.User user = securityProperties.getUser();

        inMemoryUserDetailsManager.createUser(
                new AnonymousUser(
                        user.getName(),
                        passwordEncoder.encode(user.getPassword()),
                        user.getPassword(),
                        user.getRoles()
                                .stream()
                                .map(r -> new SimpleGrantedAuthority(RoleAuthority.DEFAULT_ROLE_PREFIX + r))
                                .collect(Collectors.toList())
                )
        );

        return inMemoryUserDetailsManager;
    }

    @Bean
    @ConditionalOnMissingBean(DaoAuthenticationProvider.class)
    DaoAuthenticationProvider daoAuthenticationProvider(SecurityProperties securityProperties,
                                                        PasswordEncoder passwordEncoder) {
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

        SecurityProperties.User user = securityProperties.getUser();

        inMemoryUserDetailsManager.createUser(
                new AnonymousUser(
                        user.getName(),
                        passwordEncoder.encode(user.getPassword()),
                        user.getPassword(),
                        user.getRoles()
                                .stream()
                                .map(r -> new SimpleGrantedAuthority(RoleAuthority.DEFAULT_ROLE_PREFIX + r))
                                .collect(Collectors.toList())
                )
        );

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(inMemoryUserDetailsManager);

        return daoAuthenticationProvider;
    }

    @Bean
    AnonymousUserAuthenticationProvider anonymousUserAuthenticationProvider(PasswordEncoder passwordEncoder,
                                                                            SecurityProperties securityProperties,
                                                                            RedissonClient redissonClient) {

        AnonymousUserAuthenticationProvider authenticationProvider = new AnonymousUserAuthenticationProvider(redissonClient);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        SecurityProperties.User user = securityProperties.getUser();

        authenticationProvider.createUser(
                new AnonymousUser(
                        passwordEncoder.encode(user.getPassword()),
                        UUID.randomUUID().toString(),
                        user.getRoles()
                                .stream()
                                .map(r -> new SimpleGrantedAuthority(RoleAuthority.DEFAULT_ROLE_PREFIX + AnonymousUser.DEFAULT_ANONYMOUS_USERNAME))
                                .collect(Collectors.toList())
                )
        );

        return authenticationProvider;
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
}
