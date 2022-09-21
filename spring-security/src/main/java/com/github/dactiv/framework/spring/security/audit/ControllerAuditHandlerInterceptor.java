package com.github.dactiv.framework.spring.security.audit;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.security.audit.Auditable;
import com.github.dactiv.framework.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
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
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 控制器审计方法拦截器
 *
 * @author maurice
 */
public class ControllerAuditHandlerInterceptor implements ApplicationEventPublisherAware, AsyncHandlerInterceptor {

    private static final String DEFAULT_SUCCESS_SUFFIX_NAME = "SUCCESS";

    private static final String DEFAULT_FAILURE_SUFFIX_NAME = "FAILURE";

    private static final String DEFAULT_EXCEPTION_KEY_NAME = "exception";

    public static final String DEFAULT_HEADER_KEY = "header";

    public static final String DEFAULT_PARAM_KEY = "parameter";

    public static final String DEFAULT_BODY_KEY = "body";

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
        Object principal;

        Auditable auditable = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Auditable.class);
        if (auditable != null) {
            principal = getPrincipal(auditable.principal(), request);
            type = auditable.type();
        } else {
            Plugin plugin = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Plugin.class);
            // 如果控制器方法带有 plugin 注解并且 audit 为 true 是，记录审计内容
            if (Objects.isNull(plugin) || !plugin.audit()) {
                return ;
            }

            principal = getPrincipal(null, request);
            type = plugin.name();
            Plugin root = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Plugin.class);
            if (root != null) {
                type = root.name() + ":" + type;
            }
        }

        AuditEvent auditEvent = createAuditEvent(principal, type, request, response, handler, ex);

        // 推送审计事件
        applicationEventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));

    }

    private AuditEvent createAuditEvent(Object principal,
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
        if (SecurityUserDetails.class.isAssignableFrom(principal.getClass())) {
            SecurityUserDetails securityUserDetails = Casts.cast(principal, SecurityUserDetails.class);

            PluginAuditEvent auditEvent = new PluginAuditEvent(
                    Instant.now(),
                    securityUserDetails.getUsername(),
                    type,
                    data
            );
            auditEvent.setPrincipalId(securityUserDetails.getId().toString());
            auditEvent.setPrincipalType(securityUserDetails.getType());
            auditEvent.setMeta(securityUserDetails.getMeta());

            return auditEvent;
        } else {
            return new AuditEvent(Instant.now(), principal.toString(), type, data);
        }
    }

    private Map<String, Object> getData(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Map<String, Object> data = new LinkedHashMap<>();

        if (request.getHeaderNames().hasMoreElements()) {
            Map<String, Object> header = new LinkedHashMap<>();
            Iterator<String> iterator = request.getHeaderNames().asIterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                header.put(key, request.getHeader(key));
            }
            data.put(DEFAULT_HEADER_KEY, header);
        }

        Map<String, String[]> parameterMap = request.getParameterMap();

        if (!parameterMap.isEmpty()) {
            data.put(DEFAULT_PARAM_KEY, parameterMap);
        }

        Object body = SpringMvcUtils.getRequestAttribute(RequestBodyAttributeAdviceAdapter.REQUEST_BODY_ATTRIBUTE_NAME);
        if (Objects.nonNull(body)) {
            data.put(DEFAULT_BODY_KEY, body);
        }

        return data;
    }

    private Object getPrincipal(String key, HttpServletRequest request) {

        SecurityContext securityContext = SecurityContextHolder.getContext();

        if (securityContext.getAuthentication() == null || !securityContext.getAuthentication().isAuthenticated()) {
            String principal = null;

            if (StringUtils.isNotBlank(key)) {
                principal = request.getParameter(key);

                if (StringUtils.isBlank(principal)) {
                    principal = request.getHeader(key);
                }
            }

            if (StringUtils.isBlank(principal)) {
                principal = request.getRemoteAddr();
            }

            return principal;

        } else {
            Authentication authentication = securityContext.getAuthentication();
            return authentication.getDetails();
        }
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
