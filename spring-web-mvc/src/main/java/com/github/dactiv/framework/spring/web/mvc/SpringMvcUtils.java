package com.github.dactiv.framework.spring.web.mvc;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * spring mvc 工具类
 *
 * @author maurice.chen
 **/
public class SpringMvcUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcUtils.class);

    private final static String UNKNOWN_STRING = "unknown";

    private final static Integer IP_MIN_LENGTH = 15;

    /**
     * @deprecated 使用 {@link Casts#COMMA} 替代
     */
    @Deprecated
    public final static String COMMA_STRING = Casts.COMMA;

    public final static String DEFAULT_ATTACHMENT_NAME = "attachment;filename=";

    public final static String ANT_PATH_MATCH_ALL = "/**";

    public final static String HTTP_PROTOCOL_PREFIX = "http://";

    public final static String HTTPS_PROTOCOL_PREFIX = "https://";

    /**
     * 获取 request 的 attribute
     *
     * @param name attribute 名称
     * @param <T>  attribute 类型
     *
     * @return attribute 值
     */
    public static <T> T getRequestAttribute(String name) {
        return Casts.cast(getCurrentRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_REQUEST));
    }

    /**
     * 设置 session 的 attribute
     *
     * @param name  attribute 名称
     * @param value 值
     */
    public static void setSessionAttribute(String name, Object value) {
        getCurrentRequestAttributes().setAttribute(name, value, RequestAttributes.SCOPE_SESSION);
    }

    /**
     * 设置 request 的 attribute
     *
     * @param name  attribute 名称
     * @param value 值
     */
    public static void setRequestAttribute(String name, Object value) {
        getCurrentRequestAttributes().setAttribute(name, value, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 获取 session 的 attribute
     *
     * @param name attribute 名称
     * @param <T>  attribute 类型
     *
     * @return attribute 值
     */
    public static <T> T getSessionAttribute(String name) {
        return Casts.cast(getCurrentRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_SESSION));
    }

    /**
     * 获取 spring mvc RequestAttributes
     *
     * @return RequestAttributes
     */
    public static RequestAttributes getCurrentRequestAttributes() {
        return RequestContextHolder.currentRequestAttributes();
    }

    /**
     * 获取 HttpServletRequest
     *
     * @return httpServletRequest
     */
    public static Optional<HttpServletRequest> getHttpServletRequest() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return Optional.of(servletRequestAttributes.getRequest());
        }

        return Optional.empty();
    }

    /**
     * 获取 HttpServletResponse
     *
     * @return httpServletResponse
     */
    public static Optional<HttpServletResponse> getHttpServletResponse() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return Optional.ofNullable(servletRequestAttributes.getResponse());
        }

        return Optional.empty();
    }

    /**
     * 获取 http 响应状态
     *
     * @param httpServletResponse http servlet response
     *
     * @return http 响应状态
     */
    public static HttpStatus getHttpStatus(HttpServletResponse httpServletResponse) {
        HttpStatus result;
        try {
            result = HttpStatus.valueOf(httpServletResponse.getStatus());
        } catch (Exception e) {
            result = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return result;
    }

    /**
     * 获取当前设备
     *
     * @return 设备
     */
    public static UserAgent getCurrentDevice() {
        return DeviceUtils.getCurrentDevice(RequestContextHolder.currentRequestAttributes());
    }

    /**
     * 获取当前设备,如果后去不了，抛出异常
     *
     * @return 设备
     */
    public static UserAgent getRequiredCurrentDevice() {

        Optional<HttpServletRequest> optional = getHttpServletRequest();

        return DeviceUtils.getRequiredCurrentDevice(optional.orElseThrow(() -> new SystemException("当前线程中无法获取 HttpServletRequest 信息")));
    }

    /**
     * 获取设备唯一识别
     *
     * @return 设备唯一识别
     */
    public static String getDeviceIdentified() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();
        return getDeviceIdentified(optional.orElseThrow(() -> new SystemException("当前线程中无法获取 HttpServletRequest 信息")));
    }

    /**
     * 获取设备唯一识别
     *
     * @param request http servlet request
     *
     * @return 设备唯一识别
     */
    public static String getDeviceIdentified(HttpServletRequest request) {

        String deviceIdentified = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isBlank(deviceIdentified)) {
            deviceIdentified = request.getParameter(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME);
        }

        if (StringUtils.isBlank(deviceIdentified)) {
            deviceIdentified = request.getSession().getId();
        }

        return deviceIdentified;
    }

    /**
     * 获取请求的头里的设备唯一识别
     *
     * @return 唯一识别
     */
    public static String getRequestHeaderDeviceIdentified() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();

        return optional
                .map(request -> request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME))
                .orElse(null);

    }

    /**
     * 通过 rest 结果集构造下载类型的 ResponseEntity
     *
     * @param result rest 结果集
     *
     * @return 下载类型的 ResponseEntity
     */
    public static ResponseEntity<byte[]> createDownloadResponseEntity(RestResult<byte[]> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(SpringMvcUtils.DEFAULT_ATTACHMENT_NAME, URLEncoder.encode(result.getMessage(), Charset.defaultCharset()));
        return new ResponseEntity<>(result.getData(), headers, HttpStatus.OK);
    }

    /**
     * 创建下载类型的 ResponseEntity
     *
     * @param filename 下载文件名称
     * @param path     文件路径
     *
     * @return 下载类型的 ResponseEntity
     *
     * @throws IOException 获取路径文件失败抛出
     */
    public static ResponseEntity<byte[]> createDownloadResponseEntity(String filename, String path) throws IOException {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(DEFAULT_ATTACHMENT_NAME, URLEncoder.encode(filename, Charset.defaultCharset()));

        return new ResponseEntity<>(FileCopyUtils.copyToByteArray(new File(path)), headers, HttpStatus.CREATED);
    }

    /**
     * 获取 ip 地址
     *
     * @return ip 地址
     */
    public static String getIpAddress() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();
        return optional.map(SpringMvcUtils::getIpAddress).orElse(UNKNOWN_STRING);
    }

    /**
     * 获取 ip 地址
     *
     * @param request request http servlet reques
     *
     * @return ip 地址
     */
    public static String getIpAddress(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        //使用代理，则获取第一个IP地址
        if (StringUtils.isNotBlank(ip) && ip.length() > IP_MIN_LENGTH) {
            if (ip.indexOf(COMMA_STRING) > 0) {
                ip = ip.substring(0, ip.indexOf(COMMA_STRING));
            }
        }

        return ip;
    }

    /**
     * 获取 mac 地址
     *
     * @return  mac 地址
     */
    public static String getMacAddress() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();
        return optional.map(SpringMvcUtils::getMacAddress).orElse(UNKNOWN_STRING);
    }

    /**
     * 获取 mac 地址
     *
     * @param request  mac 地址
     *
     * @return  mac 地址
     */
    public static String getMacAddress(HttpServletRequest request) {

        try {
            InetAddress ipAddress = InetAddress.getByName(request.getRemoteAddr());
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ipAddress);
            byte[] macAddressBytes = networkInterface.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < macAddressBytes.length; i++) {
                sb.append(String.format("%02X%s", macAddressBytes[i], (i < macAddressBytes.length - 1) ? Casts.NEGATIVE : StringUtils.EMPTY));
            }
            return sb.toString();
        } catch (Exception e) {
            LOGGER.warn("获取 mac 地址出错", e);
            return UNKNOWN_STRING;
        }

    }

}
