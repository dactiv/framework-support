package com.github.dactiv.framework.spring.web.device;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.basjes.parse.useragent.UserAgent;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 设备解析请求 filter, 用于通过 HttpServletRequest 获取设备信息
 *
 * @author maurice
 */
public class DeviceResolverRequestFilter extends OncePerRequestFilter implements Ordered {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        UserAgent device = DeviceUtils.getDevice(request);
        request.setAttribute(DeviceUtils.CURRENT_DEVICE_ATTRIBUTE, device);
        filterChain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
