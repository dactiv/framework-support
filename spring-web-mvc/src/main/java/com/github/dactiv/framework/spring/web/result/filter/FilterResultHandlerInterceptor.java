package com.github.dactiv.framework.spring.web.result.filter;

import com.github.dactiv.framework.spring.web.SpringWebSupportProperties;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 过滤结果集拦截器实现
 *
 * @author maurice.chen
 */
public class FilterResultHandlerInterceptor extends HandlerInterceptorAdapter {


    private static final Logger LOGGER = LoggerFactory.getLogger(FilterResultHandlerInterceptor.class);

    /**
     * 默认过滤属性的 id 头名称
     */
    public static final String DEFAULT_FILTER_PROPERTY_ID_HEADER_NAME = "X-EXCLUDE-PROPERTY-ID";

    /**
     * 默认过滤属性的 id 参数名称
     */
    public static final String DEFAULT_FILTER_PROPERTY_ID_PARAM_NAME = "excludePropertyId";

    private final SpringWebSupportProperties properties;

    public FilterResultHandlerInterceptor(SpringWebSupportProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String id = request.getHeader(properties.getFilterPropertyIdHeaderName());

        if (StringUtils.isEmpty(id)) {
            id = request.getParameter(properties.getFilterPropertyIdParamName());
        }

        if (StringUtils.isNotEmpty(id)) {

            FilterResultHolder.set(id);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("本次请求需要使用排除值 ID 为 [{}] 的响应排除", id);
            }

        }

        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        FilterResultHolder.clear();
        super.afterCompletion(request, response, handler, ex);
    }

}
