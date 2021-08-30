package com.github.dactiv.framework.spring.security;

import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.JsonAuthenticationFailureHandler;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.provider.RequestAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "authentication.spring.security", value = "override-http-security", matchIfMissing = true)
public class WebSecurityDefaultConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private DeviceIdContextRepository deviceIdContextRepository;

    @Autowired
    private RequestAuthenticationProvider requestAuthenticationProvider;

    @Autowired
    private AuthenticationProperties properties;

    @Autowired
    private AccessDecisionManager accessDecisionManager;

    @Autowired
    private JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

    @Override
    protected void configure(AuthenticationManagerBuilder managerBuilder) {
        managerBuilder.authenticationProvider(requestAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        installHttpSecurity(http, properties, deviceIdContextRepository, accessDecisionManager);
        createRequestAuthenticationFilter(properties, jsonAuthenticationFailureHandler);
        http.addFilter(createRequestAuthenticationFilter(properties, jsonAuthenticationFailureHandler));
    }

    /**
     * 创建请求认证 filter
     *
     * @param properties 配置信息
     * @param jsonAuthenticationFailureHandler 响应 json 数据的认证失败处理实现
     *
     * @return 请求认证 filter
     */
    public static RequestAuthenticationFilter createRequestAuthenticationFilter(AuthenticationProperties properties,
                                                                                JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler) {
        RequestAuthenticationFilter requestAuthenticationFilter = new RequestAuthenticationFilter(properties);
        requestAuthenticationFilter.setAuthenticationFailureHandler(jsonAuthenticationFailureHandler);
        return requestAuthenticationFilter;
    }

    /**
     * 初始化 http security
     *
     * @param http http security
     * @param properties 安全配置信息
     * @param deviceIdContextRepository 设备唯一识别的安全上下文仓库
     * @param accessDecisionManager 访问空值管理
     *
     * @throws Exception 构造出错时抛出
     */
    public static void installHttpSecurity(HttpSecurity http,
                                    AuthenticationProperties properties,
                                    DeviceIdContextRepository deviceIdContextRepository,
                                    AccessDecisionManager accessDecisionManager) throws Exception {

        http.authorizeRequests()
                .antMatchers(properties.getAntMatchers().toArray(new String[0]))
                .permitAll()
                .anyRequest()
                .authenticated()
                .accessDecisionManager(accessDecisionManager)
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
    }
}
