package com.github.dactiv.framework.spring.security.authentication;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.web.result.error.ErrorResultResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * rest 结果集认证入口点实现
 *
 * @author maurice.chen
 */
public class RestResultAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String ERROR_INTERNAL_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

    private final List<ErrorResultResolver> resultResolvers;

    public RestResultAuthenticationEntryPoint(List<ErrorResultResolver> resultResolvers) {
        this.resultResolvers = resultResolvers;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {

        RestResult<Object> result;
        Throwable throwable = Casts.cast(request.getAttribute(ERROR_INTERNAL_ATTRIBUTE));

        if (Objects.nonNull(throwable)) {
            Optional<ErrorResultResolver> optional = resultResolvers
                    .stream()
                    .filter(r -> r.isSupport(throwable))
                    .findFirst();

            if (optional.isPresent()) {
                result = optional.get().resolve(throwable);
            } else {
                result = RestResult.ofException(throwable);
            }
        } else if (InsufficientAuthenticationException.class.isAssignableFrom(e.getClass())) {
            result = RestResult.of(HttpStatus.UNAUTHORIZED.getReasonPhrase(), HttpStatus.UNAUTHORIZED.value(), String.valueOf(HttpStatus.UNAUTHORIZED.value()));
        } else {
            result = RestResult.ofException(e);
        }

        response.setStatus(result.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(Casts.writeValueAsString(result));
    }
}
