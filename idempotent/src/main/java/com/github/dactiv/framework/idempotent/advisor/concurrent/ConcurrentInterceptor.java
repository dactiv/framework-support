package com.github.dactiv.framework.idempotent.advisor.concurrent;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.LockType;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.idempotent.exception.ConcurrentException;
import com.github.dactiv.framework.idempotent.generator.ValueGenerator;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
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
    private final ValueGenerator valueGenerator;

    public ConcurrentInterceptor(RedissonClient redissonClient, ValueGenerator valueGenerator) {
        this.redissonClient = redissonClient;
        this.valueGenerator = valueGenerator;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Concurrent concurrent = AnnotationUtils.findAnnotation(invocation.getMethod(), Concurrent.class);

        if (Objects.isNull(concurrent)) {
            return invocation.proceed();
        }

        String key = concurrent.value();

        if (StringUtils.isEmpty(key)) {
            Method method = invocation.getMethod();
            key = method.getDeclaringClass().getName() + Casts.DEFAULT_DOT_SYMBOL + method.getName();
        }

        Object concurrentKey = valueGenerator.generate(key, invocation.getMethod(), invocation.getArguments());

        RLock lock;

        if (LockType.FairLock.equals(concurrent.type())) {
            lock = redissonClient.getFairLock(concurrentKey.toString());
        } else if (LockType.Lock.equals(concurrent.type())) {
            lock = redissonClient.getLock(concurrentKey.toString());
        } else {
            throw new SystemException("找不到对 [" + concurrent.type() + "] 的所类型支持");
        }

        boolean tryLock;

        long waitTime = concurrent.waitTime().unit().toMillis(concurrent.waitTime().value());

        if (waitTime <= 0) {
            tryLock = lock.tryLock();
        } else {
            tryLock = lock.tryLock(waitTime, concurrent.leaseTime().value(), concurrent.leaseTime().unit());
        }

        if (tryLock) {
            try {
                return invocation.proceed();
            } finally {
                lock.unlock();
            }
        }

        throw new ConcurrentException(concurrent.exception());

    }

}
