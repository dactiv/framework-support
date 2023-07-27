package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.spring.security.authentication.*;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.framework.spring.security.plugin.PluginSourceTypeVoter;
import com.github.dactiv.framework.spring.web.result.error.ErrorResultResolver;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.stream.Collectors;

/**
 * spring security 配置实现
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebSecurityDefaultConfigurerAdapter {

    private final AccessTokenContextRepository accessTokenContextRepository;

    private final AuthenticationProperties properties;

    private final JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

    private final JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler;

    private final CookieRememberService cookieRememberService;

    private final AuthenticationManager authenticationManager;

    private final ApplicationEventPublisher eventPublisher;

    private final List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers;

    private final List<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapters;

    private final List<UserDetailsService> userDetailsServices;

    private final List<ErrorResultResolver> resultResolvers;

    public WebSecurityDefaultConfigurerAdapter(AccessTokenContextRepository accessTokenContextRepository,
                                               AuthenticationProperties properties,
                                               JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler,
                                               JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler,
                                               ApplicationEventPublisher eventPublisher,
                                               AuthenticationManager authenticationManager,
                                               CookieRememberService cookieRememberService,
                                               ObjectProvider<UserDetailsService> userDetailsServices,
                                               ObjectProvider<ErrorResultResolver> resultResolvers,
                                               ObjectProvider<AuthenticationTypeTokenResolver> authenticationTypeTokenResolver,
                                               ObjectProvider<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapter) {
        this.accessTokenContextRepository = accessTokenContextRepository;
        this.properties = properties;
        this.jsonAuthenticationFailureHandler = jsonAuthenticationFailureHandler;
        this.jsonAuthenticationSuccessHandler = jsonAuthenticationSuccessHandler;
        this.eventPublisher = eventPublisher;
        this.authenticationManager = authenticationManager;
        this.cookieRememberService = cookieRememberService;
        this.authenticationTypeTokenResolvers = authenticationTypeTokenResolver.stream().collect(Collectors.toList());
        this.userDetailsServices = userDetailsServices.stream().collect(Collectors.toList());
        this.webSecurityConfigurerAfterAdapters = webSecurityConfigurerAfterAdapter.stream().collect(Collectors.toList());
        this.resultResolvers = resultResolvers.stream().collect(Collectors.toList());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .authorizeHttpRequests()
                .antMatchers(properties.getPermitUriAntMatchers().toArray(new String[0]))
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .disable()
                .formLogin()
                .disable()
                .logout()
                .disable()
                .rememberMe()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestResultAuthenticationEntryPoint(resultResolvers))
                .and()
                .cors()
                .disable()
                .csrf()
                .disable()
                .requestCache()
                .disable()
                .securityContext()
                .securityContextRepository(accessTokenContextRepository);

        if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
            for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                a.configure(httpSecurity);
            }
        }

        RequestAuthenticationFilter filter = new RequestAuthenticationFilter(
                properties,
                authenticationTypeTokenResolvers,
                userDetailsServices
        );

        filter.setAuthenticationManager(authenticationManager);
        filter.setApplicationEventPublisher(eventPublisher);
        filter.setRememberMeServices(cookieRememberService);
        filter.setAuthenticationSuccessHandler(jsonAuthenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(jsonAuthenticationFailureHandler);
        //filter.setSecurityContextRepository(accessTokenContextRepository);

        httpSecurity.addFilter(filter);
        httpSecurity.addFilterBefore(new IpAuthenticationFilter(this.properties), RequestAuthenticationFilter.class);
        addConsensusBasedToMethodSecurityInterceptor(httpSecurity, properties);
        return httpSecurity.build();
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
                for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                    a.configure(web);
                }
            }
        };
    }

    /**
     * 添加 ConsensusBased 访问管理器到方法拦截器中
     *
     * @param http http security
     */
    public static void addConsensusBasedToMethodSecurityInterceptor(HttpSecurity http,
                                                                    AuthenticationProperties properties) {
        try {
            MethodSecurityInterceptor methodSecurityInterceptor = http
                    .getSharedObject(ApplicationContext.class)
                    .getBean(MethodSecurityInterceptor.class);

            AccessDecisionManager accessDecisionManager = methodSecurityInterceptor.getAccessDecisionManager();

            if (AbstractAccessDecisionManager.class.isAssignableFrom(accessDecisionManager.getClass())) {

                AbstractAccessDecisionManager adm = (AbstractAccessDecisionManager) accessDecisionManager;
                adm.getDecisionVoters().add(new PluginSourceTypeVoter());

                ConsensusBased consensusBased = new ConsensusBased(adm.getDecisionVoters());
                consensusBased.setAllowIfEqualGrantedDeniedDecisions(properties.isAllowIfEqualGrantedDeniedDecisions());

                methodSecurityInterceptor.setAccessDecisionManager(consensusBased);
            }
        } catch (Exception ignored) {

        }
    }

    /*@Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor pluginAuthorizationMethodInterceptor(ObjectProvider<SecurityContextHolderStrategy> strategyProvider,
                                                        ObjectProvider<AuthorizationEventPublisher> eventPublisherProvider) {

        AuthorizationManagerBeforeMethodInterceptor interceptor = new AuthorizationManagerBeforeMethodInterceptor(
                Pointcuts.union(new AnnotationMatchingPointcut(null, Plugin.class, true),
                        new AnnotationMatchingPointcut(Plugin.class, true)), new PluginSourceAuthorizationManager());
        interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder() + 1);

        strategyProvider.ifAvailable(interceptor::setSecurityContextHolderStrategy);
        eventPublisherProvider.ifAvailable(interceptor::setAuthorizationEventPublisher);

        return interceptor;
    }*/

}
