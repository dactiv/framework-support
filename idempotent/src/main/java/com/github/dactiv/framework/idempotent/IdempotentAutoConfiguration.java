package com.github.dactiv.framework.idempotent;

import com.github.dactiv.framework.idempotent.advisor.IdempotentInterceptor;
import com.github.dactiv.framework.idempotent.advisor.IdempotentPointcutAdvisor;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentPointcutAdvisor;
import com.github.dactiv.framework.idempotent.generator.SpelExpressionValueGenerator;
import com.github.dactiv.framework.idempotent.generator.ValueGenerator;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
    @ConditionalOnMissingBean(ConcurrentInterceptor.class)
    ConcurrentInterceptor concurrentInterceptor(RedissonClient redissonClient, ValueGenerator keyGenerator) {
        return new ConcurrentInterceptor(redissonClient, keyGenerator);
    }

    @Bean
    ConcurrentPointcutAdvisor concurrentPointcutAdvisor(ConcurrentInterceptor concurrentInterceptor) {
        return new ConcurrentPointcutAdvisor(concurrentInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean(IdempotentInterceptor.class)
    IdempotentInterceptor idempotentInterceptor(RedissonClient redissonClient, ValueGenerator keyGenerator) {
        return new IdempotentInterceptor(redissonClient, keyGenerator);
    }

    @Bean
    IdempotentPointcutAdvisor idempotentPointcutAdvisor(IdempotentInterceptor idempotentInterceptor) {
        return new IdempotentPointcutAdvisor(idempotentInterceptor);
    }

}
