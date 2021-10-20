package com.github.dactiv.framework.spring.security;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

/**
 * web security config 的后续适配器，用于在构造好 spring security 后的自定义扩展使用
 *
 * @author maurice.chen
 */
public interface WebSecurityConfigurerAfterAdapter {

    /**
     * 配置认证管理
     *
     * @param managerBuilder 认证管理绑定器
     */
    default void configure(AuthenticationManagerBuilder managerBuilder) throws Exception {

    }

    /**
     * 配置 http 访问安全
     *
     * @param httpSecurity http 访问安全
     */
    default void configure(HttpSecurity httpSecurity) throws Exception {

    }

    /**
     * 配置 web 安全
     *
     * @param web web 安全
     */
    default void configure(WebSecurity web) throws Exception {

    }
}
