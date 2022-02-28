package com.github.dactiv.framework.nacos.event;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;

/**
 * 服务监听器实现
 *
 * @author maurice.chen
 */
public class NacosServiceEventListener implements EventListener {
    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 最后访问时间
     */
    private Date lastAccessTime = new Date();

    /**
     * 过去时间
     */
    private final TimeProperties expirationTime;

    /**
     * 服务信息
     */
    private final Service service;

    /**
     * spring 事件推送着
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    public NacosServiceEventListener(TimeProperties expirationTime,
                                     Service service,
                                     ApplicationEventPublisher applicationEventPublisher) {
        this.expirationTime = expirationTime;
        this.service = service;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void onEvent(Event event) {
        if (!NamingEvent.class.isAssignableFrom(event.getClass())) {
            return;
        }

        NamingEvent namingEvent = Casts.cast(event);

        NacosService nacosService = Casts.of(service, NacosService.class);
        nacosService.setInstances(namingEvent.getInstances());

        applicationEventPublisher.publishEvent(new NacosInstancesChangeEvent(nacosService));
    }

    /**
     * 设置创建时间
     *
     * @param creationTime 创建时间
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * 获取最后访问时间
     *
     * @return 最后访问时间
     */
    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * 设置最后访问时间
     *
     * @param lastAccessTime 最后访问时间
     */
    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    /**
     * 获取服务信息
     *
     * @return 服务信息
     */
    public Service getService() {
        return service;
    }

    /**
     * 设置监听器为超时
     */
    public void expired() {
        setLastAccessTime(new Date(System.currentTimeMillis() - expirationTime.toMillis()));
    }

    /**
     * 是否过期
     *
     * @return true 是，否则 false
     */
    public boolean isExpired() {
        return new Date().getTime() - lastAccessTime.getTime() >= expirationTime.toMillis();
    }
}
