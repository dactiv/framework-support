package com.github.dactiv.framework.nacos.event;

import org.springframework.context.ApplicationEvent;

/**
 * 服务订阅事件
 *
 * @author maurice.chen
 */
public class NacosServiceSubscribeEvent extends ApplicationEvent {

    private static final long serialVersionUID = -7678924793531762050L;

    public NacosServiceSubscribeEvent(NacosService nacosService) {
        super(nacosService);
    }

}
