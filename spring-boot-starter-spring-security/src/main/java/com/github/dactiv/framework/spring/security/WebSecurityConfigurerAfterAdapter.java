package com.github.dactiv.framework.spring.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

/**
 * web security config 的后续适配器，用于在构造好 spring security 后的自定义扩展使用
 *
 * @author maurice.chen
 */
public interface WebSecurityConfigurerAfterAdapter {

    /**
     * 配置 http 访问安全
     *
     * @param httpSecurity http 访问安全
     */
    default void configure(HttpSecurity httpSecurity) {

    }

    /**
     * 配置 web 安全
     *
     * @param web web 安全
     */
    default void configure(WebSecurity web) {

    }
}
