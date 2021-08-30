package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private List<JsonAuthenticationFailureResponse> failureResponses;

    public JsonAuthenticationFailureHandler(List<JsonAuthenticationFailureResponse> failureResponses) {
        this.failureResponses = failureResponses;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException e) throws IOException, ServletException {

        RestResult<Map<String, Object>> result = RestResult.ofException(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                e,
                new LinkedHashMap<>()
        );

        failureResponses.forEach(f -> f.setting(result, request));

        response.setStatus(result.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(Casts.writeValueAsString(result));
    }

}
