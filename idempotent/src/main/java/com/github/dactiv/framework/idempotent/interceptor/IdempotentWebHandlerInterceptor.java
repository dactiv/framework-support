package com.github.dactiv.framework.idempotent.interceptor;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.idempotent.advisor.IdempotentInterceptor;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.idempotent.generator.ValueGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * spring mvc 拦截器的幂等实现
 *
 * @author maurice.chen
 */
public class IdempotentWebHandlerInterceptor extends HandlerInterceptorAdapter {

    private final IdempotentInterceptor idempotentInterceptor;

    private final Map<Class<?>, Set<Method>> initBinderCache = new ConcurrentHashMap<>(64);

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();

    private final WebBindingInitializer webBindingInitializer = new ConfigurableWebBindingInitializer();

    public IdempotentWebHandlerInterceptor(IdempotentInterceptor idempotentInterceptor) {
        this.idempotentInterceptor = idempotentInterceptor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            return super.preHandle(request, response, handler);
        }

        HandlerMethod handlerMethod = Casts.cast(handler, HandlerMethod.class);

        Idempotent idempotent = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Idempotent.class);

        if (Objects.isNull(idempotent)) {
            return true;
        }

        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
        mavContainer.setIgnoreDefaultModelOnRedirect(false);

        MethodParameter[] parameters = handlerMethod.getMethodParameters();
        Object[] args = new Object[parameters.length];

        WebDataBinderFactory dataBinderFactory = getDataBinderFactory(handlerMethod);

        ServletWebRequest webRequest = new ServletWebRequest(request, response);

        for (int i = 0; i < parameters.length; i++) {

            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);

            if (!this.resolvers.supportsParameter(parameter)) {
                throw new IllegalStateException("无法解析 [" + parameter.getExecutable().toGenericString() +
                        "] 里的第 [" + parameter.getParameterIndex() + "] 个参数内容");
            }

            args[i] = this.resolvers.resolveArgument(parameter, mavContainer, webRequest, dataBinderFactory);
        }

        if (idempotentInterceptor.isIdempotent(idempotent, handlerMethod.getMethod(), args)) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "请求过于频繁，请稍后在试。");
            return false;
        }

        return true;
    }

    private WebDataBinderFactory getDataBinderFactory(HandlerMethod handlerMethod) {

        Class<?> handlerType = handlerMethod.getBeanType();
        Set<Method> methods = this.initBinderCache.get(handlerType);

        if (methods == null) {
            methods = MethodIntrospector.selectMethods(handlerType, RequestMappingHandlerAdapter.INIT_BINDER_METHODS);
            this.initBinderCache.put(handlerType, methods);
        }

        List<InvocableHandlerMethod> initBinderMethods = new ArrayList<>();

        for (Method method : methods) {
            Object bean = handlerMethod.getBean();
            initBinderMethods.add(createInitBinderMethod(bean, method));
        }

        return new ServletRequestDataBinderFactory(initBinderMethods, webBindingInitializer);
    }

    private InvocableHandlerMethod createInitBinderMethod(Object bean, Method method) {
        InvocableHandlerMethod binderMethod = new InvocableHandlerMethod(bean, method);

        binderMethod.setHandlerMethodArgumentResolvers(this.resolvers);
        binderMethod.setDataBinderFactory(new DefaultDataBinderFactory(this.webBindingInitializer));
        binderMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

        return binderMethod;
    }

}
