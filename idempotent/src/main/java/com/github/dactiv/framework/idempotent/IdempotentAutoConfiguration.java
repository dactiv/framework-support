package com.github.dactiv.framework.idempotent;

import com.github.dactiv.framework.idempotent.advisor.IdempotentInterceptor;
import com.github.dactiv.framework.idempotent.advisor.IdempotentPointcutAdvisor;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentPointcutAdvisor;
import com.github.dactiv.framework.idempotent.generator.SpelExpressionValueGenerator;
import com.github.dactiv.framework.idempotent.generator.ValueGenerator;
import com.github.dactiv.framework.idempotent.interceptor.IdempotentWebHandlerInterceptor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.support.WebBindingInitializer;


/**
 * 幂等性自动配置
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@ConditionalOnProperty(prefix = "dactiv.idempotent", value = "enabled", matchIfMissing = true)
public class IdempotentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ValueGenerator.class)
    ValueGenerator keyGenerator() {
        return new SpelExpressionValueGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(ConcurrentPointcutAdvisor.class)
    ConcurrentPointcutAdvisor concurrentPointcutAdvisor(RedissonClient redissonClient, ValueGenerator keyGenerator) {
        return new ConcurrentPointcutAdvisor(new ConcurrentInterceptor(redissonClient, keyGenerator));
    }

    @Bean
    IdempotentInterceptor idempotentInterceptor(RedissonClient redissonClient, ValueGenerator keyGenerator) {
        return new IdempotentInterceptor(redissonClient, keyGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(IdempotentPointcutAdvisor.class)
    @ConditionalOnProperty(prefix = "dactiv.idempotent", name = "type", havingValue = "advisor", matchIfMissing = true)
    IdempotentPointcutAdvisor idempotentPointcutAdvisor(IdempotentInterceptor idempotentInterceptor) {
        return new IdempotentPointcutAdvisor(idempotentInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean(IdempotentWebHandlerInterceptor.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "dactiv.idempotent", name = "type", havingValue = "interceptor")
    IdempotentWebHandlerInterceptor idempotentHandlerInterceptor(IdempotentInterceptor idempotentInterceptor,
                                                                 WebBindingInitializer webBindingInitializer) {
        return new IdempotentWebHandlerInterceptor(idempotentInterceptor, webBindingInitializer);
    }
}
