package com.github.dactiv.framework.nacos.event;

import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * nacos 实例改变事件
 *
 * @author maurice.chen
 */
public class NacosInstancesChangeEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 3631201898764427564L;

    public NacosInstancesChangeEvent(NacosService nacosService) {
        super(nacosService);
    }

}
