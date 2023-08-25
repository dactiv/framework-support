package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.authentication.*;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.framework.spring.security.plugin.PluginSourceAuthorizationManager;
import com.github.dactiv.framework.spring.web.result.error.ErrorResultResolver;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.Pointcuts;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

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
        this.authenticationTypeTokenResolvers = authenticationTypeTokenResolver.stream().toList();
        this.userDetailsServices = userDetailsServices.stream().toList();
        this.webSecurityConfigurerAfterAdapters = webSecurityConfigurerAfterAdapter.stream().toList();
        this.resultResolvers = resultResolvers.stream().toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .authorizeHttpRequests()
                .requestMatchers(properties.getPermitUriAntMatchers().toArray(new String[0]))
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
        filter.setSecurityContextRepository(accessTokenContextRepository);

        httpSecurity.addFilter(filter);
        httpSecurity.addFilterBefore(new IpAuthenticationFilter(this.properties), RequestAuthenticationFilter.class);

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

    @Bean
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
    }

}
