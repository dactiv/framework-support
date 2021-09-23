package com.github.dactiv.framework.spring.web.argument;

import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import nl.basjes.parse.useragent.UserAgent;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 设备参数解析器
 *
 * @author maurice
 */
public class DeviceHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return UserAgent.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest request, WebDataBinderFactory binderFactory) {
        return DeviceUtils.getCurrentDevice(request);
    }
}
