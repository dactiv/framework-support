package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * rest 结果集认证入口点实现
 *
 * @author maurice.chen
 */
public class RestResultAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String ERROR_INTERNAL_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {


        RestResult<Map<String, Object>> result = RestResult.ofException(String.valueOf(response.getStatus()), e);
        Throwable throwable = Casts.cast(request.getAttribute(ERROR_INTERNAL_ATTRIBUTE));

        if (Objects.nonNull(throwable)) {
            result.setMessage(throwable.getMessage());
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(Casts.writeValueAsString(result));
    }
}
