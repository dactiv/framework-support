package com.github.dactiv.framework.spring.security.concurrent;

import com.github.dactiv.framework.spring.security.concurrent.annotation.Concurrent;
import com.github.dactiv.framework.spring.security.concurrent.key.KeyGenerator;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Objects;

/**
 * 并发拦截器
 *
 * @author maurice
 */
public class ConcurrentInterceptor implements MethodInterceptor {

    /**
     * redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * key 生成器
     */
    private final KeyGenerator keyGenerator;

    public ConcurrentInterceptor(RedissonClient redissonClient, KeyGenerator keyGenerator) {
        this.redissonClient = redissonClient;
        this.keyGenerator = keyGenerator;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Concurrent concurrent = AnnotationUtils.findAnnotation(invocation.getMethod(), Concurrent.class);

        if (Objects.isNull(concurrent)) {
            return invocation.proceed();
        }

        String key = concurrent.value();

        if (StringUtils.isEmpty(key)) {
            throw new ConcurrentException("并发处理的 key 不能为空");
        }

        String concurrentKey = keyGenerator.generate(key, invocation);

        RLock lock = redissonClient.getFairLock(concurrentKey);

        boolean tryLock;

        long waitTime = concurrent.waitTime();

        if (waitTime <= 0) {
            tryLock = lock.tryLock();
        } else {
            tryLock = lock.tryLock(waitTime, concurrent.leaseTime(), concurrent.unit());
        }

        if (tryLock) {
            try {
                return invocation.proceed();
            }  finally {
                lock.unlock();
            }
        }

        throw new ConcurrentException(concurrent.exceptionMessage());

    }

}
