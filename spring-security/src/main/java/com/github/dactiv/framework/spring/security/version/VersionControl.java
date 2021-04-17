package com.github.dactiv.framework.spring.security.version;

import com.github.dactiv.framework.spring.security.enumerate.ResponseStrategy;
import com.github.dactiv.framework.spring.web.mobile.DevicePlatform;

import java.lang.annotation.*;

/**
 * 版本控制注解
 *
 * @author maurice
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface VersionControl {

    /**
     * 设备
     *
     * @return 设备平台
     */
    DevicePlatform device();

    /**
     * 最小版本号
     *
     * @return 最小版本号
     */
    String minVersion();

    /**
     * 响应策略
     *
     * @return 响应策略
     */
    ResponseStrategy responseStrategy();

}
