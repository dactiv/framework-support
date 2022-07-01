package com.github.dactiv.framework.idempotent.config;

import com.github.dactiv.framework.idempotent.advisor.IdempotentInterceptor;
import com.github.dactiv.framework.idempotent.advisor.IdempotentPointcutAdvisor;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentPointcutAdvisor;
import com.github.dactiv.framework.idempotent.exception.IdempotentErrorResultResolver;
import com.github.dactiv.framework.idempotent.generator.SpelExpressionValueGenerator;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 幂等性自动配置
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableConfigurationProperties(IdempotentProperties.class)
@ConditionalOnProperty(prefix = "dactiv.idempotent", value = "enabled", matchIfMissing = true)
public class IdempotentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ConcurrentInterceptor.class)
    ConcurrentInterceptor concurrentInterceptor(RedissonClient redissonClient,
                                                IdempotentProperties idempotentProperties) {
        SpelExpressionValueGenerator generator = new SpelExpressionValueGenerator();
        generator.setPrefix(idempotentProperties.getConcurrentKeyPrefix());
        return new ConcurrentInterceptor(redissonClient, generator);
    }

    @Bean
    ConcurrentPointcutAdvisor concurrentPointcutAdvisor(ConcurrentInterceptor concurrentInterceptor) {
        return new ConcurrentPointcutAdvisor(concurrentInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean(IdempotentInterceptor.class)
    IdempotentInterceptor idempotentInterceptor(RedissonClient redissonClient,
                                                IdempotentProperties idempotentProperties) {
        SpelExpressionValueGenerator generator = new SpelExpressionValueGenerator();
        generator.setPrefix(idempotentProperties.getIdempotentKeyPrefix());
        return new IdempotentInterceptor(redissonClient, generator, idempotentProperties);
    }

    @Bean
    IdempotentPointcutAdvisor idempotentPointcutAdvisor(IdempotentInterceptor idempotentInterceptor) {
        return new IdempotentPointcutAdvisor(idempotentInterceptor);
    }

    @Bean
    IdempotentErrorResultResolver idempotentErrorResultResolver() {
        return new IdempotentErrorResultResolver();
    }

}
