package com.github.dactiv.framework.nacos;


import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.github.dactiv.framework.nacos.event.NacosDiscoveryEventProperties;
import com.github.dactiv.framework.nacos.event.NacosServiceListenerValidator;
import com.github.dactiv.framework.nacos.event.NacosSpringEventManager;
import com.github.dactiv.framework.nacos.task.NacosCronScheduledListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * nacos 自动配置类
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureAfter(NacosConfigAutoConfiguration.class)
@EnableConfigurationProperties(NacosDiscoveryEventProperties.class)
public class NacosAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(NacosCronScheduledListener.class)
    @ConditionalOnProperty(prefix = "spring.cloud.nacos.config.schedule", value = "enabled", matchIfMissing = true)
    public NacosCronScheduledListener nacosCronScheduledListener(NacosConfigManager nacosConfigManager) {
        return new NacosCronScheduledListener(nacosConfigManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.nacos.discovery.event", value = "enabled")
    public NacosSpringEventManager nacosServiceEventManager(NacosDiscoveryProperties nacosDiscoveryProperties,
                                                            NacosServiceManager nacosServiceManager,
                                                            NacosDiscoveryEventProperties nacosDiscoveryEventProperties,
                                                            List<NacosServiceListenerValidator> nacosServiceListenerValidatorList) {

        return new NacosSpringEventManager(
                nacosDiscoveryProperties,
                nacosServiceManager,
                nacosDiscoveryEventProperties,
                nacosServiceListenerValidatorList
        );

    }
}
