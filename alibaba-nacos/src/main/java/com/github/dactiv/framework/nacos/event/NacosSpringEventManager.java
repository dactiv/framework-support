package com.github.dactiv.framework.nacos.event;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * nacos 的 spring 事件管理器
 *
 * @author maurice.chen
 */
public class NacosSpringEventManager implements ApplicationEventPublisherAware, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosSpringEventManager.class);

    /**
     * nacos 服务发现事件配置
     */
    private final NacosDiscoveryEventProperties discoveryEventProperties;
    /**
     * nacos 服务发现配置
     */
    private final NacosDiscoveryProperties discoveryProperties;
    /**
     * nacos 服务管理器
     */
    private final NacosServiceManager nacosServiceManager;
    /**
     * 服务订阅校验集合
     */
    private final List<NacosServiceListenerValidator> nacosServiceListenerValidators;
    /**
     * spring 事件推送着
     */
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 所有监听的缓存，key 为服务组，值为该组下的所有服务监听器
     */
    private final Map<String, List<NacosServiceEventListener>> listenerCache = new LinkedHashMap<>();

    public NacosSpringEventManager(NacosDiscoveryProperties discoveryProperties,
                                   NacosServiceManager nacosServiceManager,
                                   NacosDiscoveryEventProperties discoveryEventProperties,
                                   List<NacosServiceListenerValidator> nacosServiceListenerValidators) {

        this.discoveryProperties = discoveryProperties;
        this.nacosServiceManager = nacosServiceManager;
        this.discoveryEventProperties = discoveryEventProperties;
        this.nacosServiceListenerValidators = nacosServiceListenerValidators;
    }

    /**
     * 超时所有监听缓存
     */
    public void expiredAllListener() {
        listenerCache
                .values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(NacosServiceEventListener::expired);
    }

    /**
     * 清除监听缓存
     */
    public void clearListenerCache() {
        listenerCache.clear();
    }

    /**
     * 扫描并取消订阅服务
     */
    @NacosCronScheduled(cron = "${spring.cloud.nacos.discovery.event.unsubscribe-service-cron:0 0/1 * * * ?}")
    public void scanThenUnsubscribeService() {

        NamingService namingService = nacosServiceManager.getNamingService(
                discoveryProperties.getNacosProperties()
        );

        List<NacosServiceEventListener> listeners = listenerCache
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        for (NacosServiceEventListener sel : listeners) {

            if (!sel.isExpired()) {
                continue;
            }
            try {

                LOGGER.info("对服务 [" + sel.getService().getName() + "] 取消订阅");

                namingService.unsubscribe(sel.getService().getName(), sel.getService().getGroupName(), sel);

                List<NacosServiceEventListener> list = listenerCache.get(sel.getService().getGroupName());
                list.remove(sel);

                applicationEventPublisher.publishEvent(new NacosServiceUnsubscribeEvent(sel.getService()));
            } catch (Exception e) {
                LOGGER.error("取消订阅 [" + sel.getService().getName() + "] 服务失败", e);
            }
        }
    }

    /**
     * 扫描并订阅服务
     */
    @NacosCronScheduled(cron = "${spring.cloud.nacos.discovery.event.subscribe-service-cron:30 0/1 * * * ?}")
    public void scanThenSubscribeService() {
        NamingService namingService = nacosServiceManager.getNamingService(
                discoveryProperties.getNacosProperties()
        );

        NamingMaintainService namingMaintainService = nacosServiceManager.getNamingMaintainService(
                discoveryProperties.getNacosProperties()
        );

        try {

            // 获取所有服务
            ListView<String> view = namingService.getServicesOfServer(1, Integer.MAX_VALUE);

            for (String s : view.getData()) {
                // 通过服务名获取服务信息
                Service service = namingMaintainService.queryService(s);
                // 通过服务名获取所有服务实例
                List<Instance> instanceList = namingService.getAllInstances(service.getName(), service.getGroupName());
                // 创建一组监听缓存，如果存在取当前的数据，否则创建一个
                List<NacosServiceEventListener> listeners = listenerCache.computeIfAbsent(
                        service.getGroupName(),
                        k -> new LinkedList<>()
                );

                Optional<NacosServiceEventListener> optional = listeners
                        .stream()
                        .filter(l -> l.getService().getName().equals(s))
                        .findFirst();
                // 如果当前服务已经被监听过，更新一次最后访问时间
                if (optional.isPresent()) {

                    optional.get().setLastAccessTime(new Date());

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[" + s + "] 服务已订阅，更新创建时间。");
                    }

                    continue;
                }
                // 创建监听器
                NacosServiceEventListener listener = new NacosServiceEventListener(
                        discoveryEventProperties.getExpireUnsubscribeTime(),
                        service,
                        applicationEventPublisher
                );

                NacosService nacosService = Casts.of(service, NacosService.class);
                nacosService.setInstances(instanceList);

                if (CollectionUtils.isNotEmpty(nacosServiceListenerValidators)) {

                    List<NacosServiceListenerValidator> validators = nacosServiceListenerValidators
                            .stream()
                            .filter(v -> v.isSupport(nacosService))
                            .collect(Collectors.toList());

                    boolean isContinue = false;

                    for (NacosServiceListenerValidator v : validators) {
                        if (!v.subscribeValid(nacosService)) {
                            isContinue = true;
                            break;
                        }
                    }

                    if (isContinue) {
                        continue;
                    }
                }

                // 添加到缓存中
                listeners.add(listener);

                LOGGER.info("订阅组为 [" + service.getGroupName() + "] 的 [" + s + "] 服务");
                // 订阅服务
                namingService.subscribe(service.getName(), service.getGroupName(), listener);
                // 推送订阅事件
                applicationEventPublisher.publishEvent(new NacosServiceSubscribeEvent(nacosService));
            }

        } catch (Exception e) {
            LOGGER.error("扫描服务信息出错", e);
        }

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void destroy() {
        LOGGER.info("解除所有服务监听");

        listenerCache
                .values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(NacosServiceEventListener::expired);

        scanThenUnsubscribeService();
    }

    /**
     * 获取 naocs 的命名服务
     *
     * @return naocs 的命名服务
     */
    public NamingService getNamingService() {
        return nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());
    }

    /**
     * 获取 naocs 的命名维护服务
     *
     * @return naocs 的命名维护服务
     */
    public NamingMaintainService getNamingMaintainService() {
        return nacosServiceManager.getNamingMaintainService(discoveryProperties.getNacosProperties());
    }

    /**
     * 监听本服务注册完成事件，当注册完成时候，同步所有插件菜单。
     *
     * @param event 事件原型
     */
    @EventListener
    public void onInstanceRegisteredEvent(InstanceRegisteredEvent<NacosAutoServiceRegistration> event) {
        scanThenSubscribeService();
    }

}
