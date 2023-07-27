package com.github.dactiv.framework.spring.security.authentication.handler;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 响应 json 数据的认证失败处理实现
 *
 * @author maurice.chen
 */
public class JsonAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final List<JsonAuthenticationSuccessResponse> successResponses;

    private final AntPathRequestMatcher loginRequestMatcher;

    public JsonAuthenticationSuccessHandler(List<JsonAuthenticationSuccessResponse> successResponses,
                                            AuthenticationProperties authenticationProperties) {
        this.successResponses = successResponses;
        this.loginRequestMatcher = new AntPathRequestMatcher(authenticationProperties.getLoginProcessingUrl(), HttpMethod.POST.name());
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain,
                                        Authentication authentication) throws IOException, ServletException {

        if (!loginRequestMatcher.matches(request)) {
            chain.doFilter(request, response);
        } else {
            onAuthenticationSuccess(request, response, authentication);
        }
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException{

        RestResult<Object> result = RestResult.of(HttpStatus.OK.getReasonPhrase());
        if (authentication.getDetails() instanceof SecurityUserDetails) {
            SecurityUserDetails userDetails = Casts.cast(authentication.getDetails());
            SecurityUserDetails returnValue = Casts.of(userDetails, SecurityUserDetails.class);
            result.setData(returnValue);
        }

        if (CollectionUtils.isNotEmpty(successResponses)) {
            successResponses.forEach(f -> f.setting(result, request));
        }


        if (loginRequestMatcher.matches(request)) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(Casts.writeValueAsString(result));
        }
    }
}
