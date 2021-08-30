package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import com.github.dactiv.framework.spring.security.plugin.PluginSourceTypeVoter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * spring security 配置实现
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebSecurityDefaultConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private DeviceIdContextRepository deviceIdContextRepository;

    @Autowired
    private RequestAuthenticationProvider requestAuthenticationProvider;

    @Autowired
    private AuthenticationProperties properties;

    @Autowired
    private JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

    @Autowired(required = false)
    private List<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapters = new LinkedList<>();

    @Override
    protected void configure(AuthenticationManagerBuilder managerBuilder) throws Exception {
        if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
            for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                a.configure(managerBuilder);
            }
        } else {
            managerBuilder.authenticationProvider(requestAuthenticationProvider);
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
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers(properties.getPermitUriAntMatchers().toArray(new String[0]))
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .disable()
                .logout()
                .disable()
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
                a.configure(http);
            }
        } else {

            RequestAuthenticationFilter requestAuthenticationFilter = new RequestAuthenticationFilter(properties);
            requestAuthenticationFilter.setAuthenticationFailureHandler(jsonAuthenticationFailureHandler);

            http.addFilter(requestAuthenticationFilter);
        }

        addConsensusBasedToMethodSecurityInterceptor(http, properties);
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

}
