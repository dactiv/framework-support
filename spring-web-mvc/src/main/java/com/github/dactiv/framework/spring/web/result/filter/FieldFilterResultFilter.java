package com.github.dactiv.framework.spring.web.result.filter;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.SpringWebMvcProperties;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 字段过滤结果集过滤器
 *
 * @author maurice.chen
 */
public class FieldFilterResultFilter extends GenericFilterBean {

    private final SpringWebMvcProperties springWebMvcProperties;

    public FieldFilterResultFilter(SpringWebMvcProperties springWebMvcProperties) {
        this.springWebMvcProperties = springWebMvcProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        try {

            String id = getFilterResultId(request);

            if (StringUtils.isNotEmpty(id)) {
                FilterResultHolder.add(id);
                if (logger.isDebugEnabled()) {
                    logger.debug("本次请求需要使用排除值 ID 为 [" + id + "] 的响应排除");
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            FilterResultHolder.clear();
            if (logger.isTraceEnabled()) {
                logger.trace("清除线程绑定的 filter result 内容: " + request);
            }
        }

    }

    /**
     * 设置过滤返回对象结果集的值
     *
     * @param request http 请求对象
     */
    public String getFilterResultId(ServletRequest request) {
        String id = "";

        if (HttpServletRequest.class.isAssignableFrom(request.getClass())) {
            HttpServletRequest httpRequest = Casts.cast(request);
            id = httpRequest.getHeader(springWebMvcProperties.getFilterResultIdHeaderName());
        }

        if (StringUtils.isEmpty(id)) {
            id = request.getParameter(springWebMvcProperties.getFilterResultIdParamName());
        }

        return id;
    }
}
