package com.github.dactiv.framework.spring.security.audit;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 控制器审计方法拦截器
 *
 * @author maurice
 */
public class ControllerAuditHandlerInterceptor extends HandlerInterceptorAdapter implements ApplicationEventPublisherAware {

    private static final String DEFAULT_SUCCESS_SUFFIX_NAME = "SUCCESS";

    private static final String DEFAULT_FAILURE_SUFFIX_NAME = "FAILURE";

    private static final String DEFAULT_EXCEPTION_KEY_NAME = "exception";

    /**
     * spring 应用的事件推送器
     */
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 成功执行的后缀名称，用与说明执行某个动作时区分成功或失败或异常
     */
    private String successSuffixName = DEFAULT_SUCCESS_SUFFIX_NAME;

    /**
     * 失败执行的后缀名称，用与说明执行某个动作时区分成功或失败或异常
     */
    private String failureSuffixName = DEFAULT_FAILURE_SUFFIX_NAME;

    /**
     * 异常执行的后缀名称，用与说明执行某个动作时区分成功或失败或异常
     */
    private String exceptionKeyName = DEFAULT_EXCEPTION_KEY_NAME;

    public ControllerAuditHandlerInterceptor() {
    }

    public ControllerAuditHandlerInterceptor(String successSuffixName,
                                             String failureSuffixName,
                                             String exceptionKeyName) {

        this.successSuffixName = successSuffixName;
        this.failureSuffixName = failureSuffixName;
        this.exceptionKeyName = exceptionKeyName;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        if (!HandlerMethod.class.isAssignableFrom(handler.getClass())) {
            return;
        }

        HandlerMethod handlerMethod = Casts.cast(handler);

        String type;

        String principal;

        Auditable auditable = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Auditable.class);

        if (auditable != null) {

            principal = getPrincipal(auditable.principal(), request);

            type = auditable.type();

        } else {
            Plugin plugin = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Plugin.class);
            // 如果控制器方法带有 plugin 注解并且 audit 为 true 是，记录审计内容
            if (plugin != null && plugin.audit()) {

                principal = getPrincipal(null, request);

                type = plugin.name();

                Plugin root = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Plugin.class);

                if (root != null) {
                    type = root.name() + ":" + type;
                }

                createAuditEvent(principal, type, request, response, handler, ex);

            } else {
                return;
            }
        }

        AuditEvent auditEvent = createAuditEvent(principal, type, request, response, handler, ex);

        // 推送审计事件
        applicationEventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));

    }

    private AuditEvent createAuditEvent(String principal,
                                        String type,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {

        Map<String, Object> data = getData(request, response, handler);

        if (ex == null && HttpStatus.OK.value() == response.getStatus()) {
            type = type + ":" + successSuffixName;
        } else {
            type = type + ":" + failureSuffixName;

            if (Objects.nonNull(ex)) {
                data.put(exceptionKeyName, ex.getMessage());
            }
        }

        return new AuditEvent(Instant.now(), principal, type, data);
    }

    private Map<String, Object> getData(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Map<String, Object> data = new LinkedHashMap<>();

        Map<String, String[]> parameterMap = request.getParameterMap();

        if (!parameterMap.isEmpty()) {
            data.putAll(parameterMap);
        }

        return data;
    }

    private String getPrincipal(String key, HttpServletRequest request) {

        String principal = null;

        SecurityContext securityContext = SecurityContextHolder.getContext();

        if (securityContext.getAuthentication() == null || !securityContext.getAuthentication().isAuthenticated()) {

            if (StringUtils.isNotBlank(key)) {
                principal = request.getParameter(key);

                if (StringUtils.isBlank(principal)) {
                    principal = request.getHeader(key);
                }
            }

            if (StringUtils.isBlank(principal)) {
                principal = request.getRemoteAddr();
            }

        } else {
            Authentication authentication = securityContext.getAuthentication();

            principal = authentication.getPrincipal().toString();

            Object detail = authentication.getDetails();

            if (SecurityUserDetails.class.isAssignableFrom(detail.getClass())) {
                SecurityUserDetails securityUserDetails = Casts.cast(detail);
                principal = securityUserDetails.getUsername();
            }
        }

        return principal;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public String getSuccessSuffixName() {
        return successSuffixName;
    }

    public String getFailureSuffixName() {
        return failureSuffixName;
    }

    public String getExceptionKeyName() {
        return exceptionKeyName;
    }

}
