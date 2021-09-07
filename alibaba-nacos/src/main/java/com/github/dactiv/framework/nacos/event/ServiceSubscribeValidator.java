package com.github.dactiv.framework.nacos.event;

/**
 * 服务订阅校验，用于是否过滤订阅服务使用
 *
 * @author maurice.chen
 */
public interface ServiceSubscribeValidator {

    /**
     * 是否支持此服务
     *
     * @param nacosService nacos 服务
     *
     * @return true 是，否则 false，返回 true 时，会触发 {@link #valid(NacosService)} 方法
     */
    boolean isSupport(NacosService nacosService);

    /**
     * 校验服务
     *
     * @param nacosService nacos 服务
     *
     * @return true 订阅服务，否则 false
     */
    boolean valid(NacosService nacosService);
}
