package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import com.github.dactiv.framework.spring.security.plugin.PluginSourceTypeVoter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * spring security ????????????
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebSecurityDefaultConfigurerAdapter extends WebSecurityConfigurerAdapter {

    private final DeviceIdContextRepository deviceIdContextRepository;

    private final RequestAuthenticationProvider requestAuthenticationProvider;

    private final AuthenticationProperties properties;

    private final JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

    private final JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler;

    private final ApplicationEventPublisher eventPublisher;

    private final AuthenticationManager authenticationManager;

    private final List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers;

    private final List<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapters;

    public WebSecurityDefaultConfigurerAdapter(DeviceIdContextRepository deviceIdContextRepository,
                                               RequestAuthenticationProvider requestAuthenticationProvider,
                                               AuthenticationProperties properties,
                                               JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler,
                                               JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler,
                                               ApplicationEventPublisher eventPublisher,
                                               AuthenticationManager authenticationManager,
                                               ObjectProvider<AuthenticationTypeTokenResolver> authenticationTypeTokenResolver,
                                               ObjectProvider<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapter) {
        this.deviceIdContextRepository = deviceIdContextRepository;
        this.requestAuthenticationProvider = requestAuthenticationProvider;
        this.properties = properties;
        this.jsonAuthenticationFailureHandler = jsonAuthenticationFailureHandler;
        this.jsonAuthenticationSuccessHandler = jsonAuthenticationSuccessHandler;
        this.eventPublisher = eventPublisher;
        this.authenticationManager = authenticationManager;
        this.authenticationTypeTokenResolvers = authenticationTypeTokenResolver.stream().collect(Collectors.toList());
        this.webSecurityConfigurerAfterAdapters = webSecurityConfigurerAfterAdapter.stream().collect(Collectors.toList());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder managerBuilder) throws Exception {
        if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
            for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                a.configure(managerBuilder);
            }
        } else {
            super.configure(managerBuilder);
        }
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
            for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                a.configure(web);
            }
        } else {
            super.configure(web);
        }
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> map = new LinkedHashMap<>();
        map.put(
                new AntPathRequestMatcher("/**"),
                (req, res, e) -> res.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase())
        );

        DelegatingAuthenticationEntryPoint authenticationEntryPoint = new DelegatingAuthenticationEntryPoint(map);
        authenticationEntryPoint.setDefaultEntryPoint(new Http403ForbiddenEntryPoint());

        httpSecurity.authorizeRequests()
                .antMatchers(properties.getPermitUriAntMatchers().toArray(new String[0]))
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .authenticationProvider(requestAuthenticationProvider)
                .httpBasic()
                .disable()
                .formLogin()
                .disable()
                .logout()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .cors()
                .disable()
                .csrf()
                .disable()
                .requestCache()
                .disable()
                .securityContext()
                .securityContextRepository(deviceIdContextRepository);

        if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
            for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                a.configure(httpSecurity);
            }
        } else {

            RequestAuthenticationFilter filter = new RequestAuthenticationFilter(
                    properties,
                    authenticationTypeTokenResolvers
            );

            filter.setAuthenticationManager(authenticationManager);
            filter.setApplicationEventPublisher(eventPublisher);
            filter.setAuthenticationSuccessHandler(jsonAuthenticationSuccessHandler);
            filter.setAuthenticationFailureHandler(jsonAuthenticationFailureHandler);

            httpSecurity.addFilter(filter);
        }

        addConsensusBasedToMethodSecurityInterceptor(httpSecurity, properties);
    }

    /**
     * ?????? ConsensusBased ????????????????????????????????????
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

}
