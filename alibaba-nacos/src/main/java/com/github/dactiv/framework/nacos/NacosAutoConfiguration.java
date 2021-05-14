package com.github.dactiv.framework.nacos;


import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.github.dactiv.framework.nacos.task.NacosCronScheduledListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(NacosConfigAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.cloud.nacos.support", value = "enabled", matchIfMissing = true)
public class NacosAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(NacosCronScheduledListener.class)
    public NacosCronScheduledListener nacosCronScheduledListener(NacosConfigManager nacosConfigManager) {
        return new NacosCronScheduledListener(nacosConfigManager);
    }
}
