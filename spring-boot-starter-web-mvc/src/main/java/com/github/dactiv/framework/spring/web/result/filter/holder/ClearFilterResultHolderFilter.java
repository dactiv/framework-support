package com.github.dactiv.framework.spring.web.result.filter.holder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * 清除过滤结果集持有者的所有数据 filter
 *
 * @author maurice.chen
 */
public class ClearFilterResultHolderFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {

            FilterResultHolder.clear();

            if (logger.isTraceEnabled()) {
                logger.trace("清除线程绑定的 filter result 内容");
            }
        }
    }
}
