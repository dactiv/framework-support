package com.github.dactiv.framework.spring.web.device;

import com.github.dactiv.framework.commons.Casts;
import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.web.context.request.RequestAttributes;

/**
 * 设备工具类
 *
 * @author maurice
 */
public class DeviceUtils {

    private static final UserAgentAnalyzer USER_AGENT_ANALYZER = UserAgentAnalyzer
            .newBuilder()
            //.withFields("DeviceClass","OperatingSystemClass")
            .hideMatcherLoadStats()
            .build();

    public static final String USER_AGENT_HEADER_NAME = "USER-AGENT";

    /**
     * 当前设备的 HttpServletRequest 属性的名称。
     */
    public static final String CURRENT_DEVICE_ATTRIBUTE = "currentDevice";
    /**
     * 请求头的设备唯一识别
     */
    public static final String REQUEST_DEVICE_IDENTIFIED_HEADER_NAME = "X-DEVICE-IDENTIFIED";
    /***
     * 请求参数的设备唯一识别
     */
    public static final String REQUEST_DEVICE_IDENTIFIED_PARAM_NAME = "deviceIdentified";

    /**
     * 从 Web 请求中提取当前设备.
     *
     * @param request the servlet request
     *
     * @return 当前设备, 如果尚未为请求解析任何设备，则为null
     */
    public static UserAgent getCurrentDevice(HttpServletRequest request) {
        return Casts.cast(request.getAttribute(CURRENT_DEVICE_ATTRIBUTE));
    }

    /**
     * 获取设备信息
     *
     * @param request the servlet request
     *
     * @return 设备信息
     */
    public static UserAgent getDevice(HttpServletRequest request) {
        return getDevice(request.getHeader(USER_AGENT_HEADER_NAME));
    }

    /**
     * 获取设备信息
     *
     * @param userAgent user agent
     *
     * @return 设备信息
     */
    public static UserAgent getDevice(String userAgent) {
        return USER_AGENT_ANALYZER.parse(userAgent);
    }

    /**
     * 从 Web 请求中提取当前设备,如果当前设备尚未解析，则抛出运行时异常。
     *
     * @param request the servlet request
     *
     * @return 当前设备
     */
    public static UserAgent getRequiredCurrentDevice(HttpServletRequest request) {
        UserAgent device = getCurrentDevice(request);
        if (device == null) {
            throw new IllegalStateException("此请求中未设置任何当前设备，您是否配置了 DeviceResolverRequestFilter ？");
        }
        return device;
    }

    /**
     * 从 spring mvc 请求属性映射中提取当前设备.
     *
     * @param attributes the request attributes
     *
     * @return 当前设备, 如果尚未为请求解析任何设备，则为null
     */
    public static UserAgent getCurrentDevice(RequestAttributes attributes) {
        return (UserAgent) attributes.getAttribute(CURRENT_DEVICE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    }
}
