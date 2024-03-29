package com.github.dactiv.framework.spring.security.authentication.handler;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 响应 json 数据的认证失败处理实现
 *
 * @author maurice.chen
 */
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final List<JsonAuthenticationFailureResponse> failureResponses;

    private final AntPathRequestMatcher loginRequestMatcher;

    public JsonAuthenticationFailureHandler(List<JsonAuthenticationFailureResponse> failureResponses,
                                            AuthenticationProperties authenticationProperties) {
        this.failureResponses = failureResponses;
        this.loginRequestMatcher = new AntPathRequestMatcher(authenticationProperties.getLoginProcessingUrl(), HttpMethod.POST.name());
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException e) throws IOException {

        if (!loginRequestMatcher.matches(request)) {
            return ;
        }

        RestResult<Map<String, Object>> result = RestResult.ofException(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                e,
                new LinkedHashMap<>()
        );

        if (CollectionUtils.isNotEmpty(failureResponses)) {
            failureResponses.forEach(f -> f.setting(result, request, e));
        }

        response.setStatus(result.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(Casts.writeValueAsString(result));
    }

}
