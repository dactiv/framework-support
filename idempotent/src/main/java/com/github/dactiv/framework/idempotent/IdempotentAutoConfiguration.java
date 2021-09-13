package com.github.dactiv.framework.idempotent;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.idempotent.advisor.IdempotentInterceptor;
import com.github.dactiv.framework.idempotent.advisor.IdempotentPointcutAdvisor;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentPointcutAdvisor;
import com.github.dactiv.framework.idempotent.generator.SpelExpressionValueGenerator;
import com.github.dactiv.framework.idempotent.generator.ValueGenerator;
import com.github.dactiv.framework.idempotent.interceptor.IdempotentWebHandlerInterceptor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;

import java.util.*;


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

}
