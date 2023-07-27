package com.github.dactiv.framework.spring.security.authentication.service;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * feign 认证错误响应处理
 *
 * @author maurice.chen
 */
public class DefaultAuthenticationFailureResponse implements JsonAuthenticationFailureResponse {

    private final AuthenticationProperties properties;

    public DefaultAuthenticationFailureResponse(AuthenticationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setting(RestResult<Map<String, Object>> result, HttpServletRequest request, AuthenticationException e) {
        String authHeader = request.getHeader(properties.getTypeHeaderName());
        if (!StringUtils.equals(authHeader,DefaultUserDetailsService.DEFAULT_TYPES)) {
            return;
        }

        result.setStatus(HttpStatus.UNAUTHORIZED.value());
        result.setExecuteCode(String.valueOf(HttpStatus.UNAUTHORIZED.value()));
    }
}
