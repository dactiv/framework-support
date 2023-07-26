package com.github.dactiv.framework.nacos.event;

import com.alibaba.nacos.api.naming.pojo.Service;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * 服务取消订阅事件
 *
 * @author maurice.chen
 */
public class NacosServiceUnsubscribeEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = -7678924793531762050L;

    public NacosServiceUnsubscribeEvent(Service service) {
        super(service);
    }

}
