package com.github.dactiv.framework.idempotent.advisor.concurrent;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.ConcurrentProperties;
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
import java.util.function.Supplier;

/**
 * 并发拦截器
 *
 * @author maurice
 */
public class ConcurrentInterceptor implements MethodInterceptor {

    public static final String DEFAULT_EXCEPTION = "执行过程中出现并发";

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

        if (StringUtils.isBlank(key)) {
            Method method = invocation.getMethod();
            key = method.getDeclaringClass().getName() + Casts.DEFAULT_DOT_SYMBOL + method.getName();
        }

        Object concurrentKey = valueGenerator.generate(key, invocation.getMethod(), invocation.getArguments());

        return invoke(concurrentKey.toString(), concurrent, () -> this.invocationProceed(invocation));

    }

    /**
     * 执行 aop 内容
     *
     * @param invocation 方法执行器
     *
     * @return 执行后的返回内容
     */
    private Object invocationProceed(MethodInvocation invocation)  {
        try {
            return invocation.proceed();
        } catch (Throwable e) {
            throw new SystemException("并发 aop 调用错误", e);
        }
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param concurrent 并发注解
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     *
     */
    private <R> R invoke(String key, Concurrent concurrent, Supplier<R> supplier) {
        TimeProperties waitTime = TimeProperties.of(concurrent.waitTime());
        TimeProperties leaseTime = TimeProperties.of(concurrent.leaseTime());

        return invoke(key, concurrent.type(), waitTime, leaseTime, concurrent.exception(), supplier);
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     */
    public <R> R invoke(String key, Supplier<R> supplier) {
        return invoke(key, LockType.Lock, supplier);
    }

    /**
     * 执行并发处理过程
     *
     * @param properties 并发配置
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     * @return 返回值
     */
    public <R> R invoke(ConcurrentProperties properties, Supplier<R> supplier) {

        return invoke(
                properties.getKey(),
                properties.getLockType(),
                properties.getWaitTime(),
                properties.getLeaseTime(),
                properties.getException(),
                supplier
        );
    }

    /***
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     */
    public <R> R invoke(String key, LockType type, Supplier<R> supplier) {
        return invoke(key, type, null, supplier);
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     *
     */
    public <R> R invoke(String key, LockType type, TimeProperties waitTime, Supplier<R> supplier) {
        return invoke(key, type, waitTime, null, supplier);
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param leaseTime 释放锁时间 (当获取到锁时候，在该时间不管执行过程供应者执行完成或不完成，都将当前锁释放)
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     */
    public <R> R invoke(String key, LockType type, TimeProperties waitTime, TimeProperties leaseTime, Supplier<R> supplier) {
        return invoke(key, type, waitTime, leaseTime, DEFAULT_EXCEPTION, supplier);
    }

    /**
     *
     * @param key 键值
     * @param type 锁类型
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param leaseTime 释放锁时间 (当获取到锁时候，在该时间不管执行过程供应者执行完成或不完成，都将当前锁释放)
     * @param exception 异常信息
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 执行过程供应者返回值
     */
    public <R> R invoke(String key, LockType type, TimeProperties waitTime, TimeProperties leaseTime, String exception, Supplier<R> supplier) {
        RLock lock = getLock(key, type);

        boolean tryLock = tryLock(lock, waitTime, leaseTime);

        if (!tryLock) {
            throw new ConcurrentException(exception);
        }

        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }

    }

    /**
     * 获取锁
     *
     * @param key 建值
     * @param type 锁类型
     *
     * @return redis 锁实现
     */
    private RLock getLock(String key, LockType type) {
        if (LockType.FairLock.equals(type)) {
            return redissonClient.getFairLock(key);
        } else if (LockType.Lock.equals(type)) {
            return redissonClient.getLock(key);
        }

        throw new SystemException("找不到对 [" + type + "] 的所类型支持");
    }

    /**
     * 尝试加锁
     *
     * @param lock 锁
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param leaseTime 释放锁时间 (当获取到锁时候，在该时间不管执行过程供应者执行完成或不完成，都将当前锁释放)
     *
     * @return true 加锁成功，否则 false
     */
    private boolean tryLock(RLock lock, TimeProperties waitTime, TimeProperties leaseTime) {

        try {
            if (Objects.nonNull(waitTime) && Objects.nonNull(leaseTime)) {
                long waitTimeValue = waitTime.getUnit().toMillis(waitTime.getValue());
                return lock.tryLock(waitTimeValue, leaseTime.getValue(), leaseTime.getUnit());
            } else if (Objects.nonNull(waitTime)) {
                return lock.tryLock(waitTime.getValue(), waitTime.getUnit());
            }
        } catch (Exception e) {
            throw new SystemException("并发获取锁时出现异常", e);
        }

        return lock.tryLock();
    }

}
