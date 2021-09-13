package com.github.dactiv.framework.idempotent.advisor;

import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.idempotent.exception.ConcurrentException;
import com.github.dactiv.framework.idempotent.exception.IdempotentException;
import com.github.dactiv.framework.idempotent.generator.ValueGenerator;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * aop 形式的幂等拦截器实现
 *
 * @author maurice
 */
public class IdempotentInterceptor implements MethodInterceptor {

    /**
     * redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * key 生成器
     */
    private final ValueGenerator valueGenerator;
    /**
     * 参数名称发现者，用于获取 Concurrent 注解下的方法参数细信息
     */
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public IdempotentInterceptor(RedissonClient redissonClient, ValueGenerator valueGenerator) {
        this.redissonClient = redissonClient;
        this.valueGenerator = valueGenerator;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Idempotent idempotent = AnnotationUtils.findAnnotation(invocation.getMethod(), Idempotent.class);

        if (Objects.isNull(idempotent)) {
            return invocation.proceed();
        }

        if (isIdempotent(idempotent, invocation.getMethod(), invocation.getArguments())) {
            throw new IdempotentException(idempotent.exception());
        }

        return invocation.proceed();

    }

    /**
     * 判断是否幂等
     *
     * @param idempotent 幂等注解
     * @param method 当前方法
     * @param arguments 参数信息
     *
     * @return true 为幂等， 否则 false
     */
    public boolean isIdempotent(Idempotent idempotent, Method method, Object[] arguments) {
        String key = idempotent.key();

        if (StringUtils.isEmpty(key)) {
            key = method.toString();
        }

        Object keyValue = valueGenerator.generate(key, method, arguments);

        List<Object> values = new LinkedList<>();

        if (ArrayUtils.isEmpty(idempotent.value())) {

            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

            List<Object> value = new LinkedList<>();

            for (int i = 0; i < (parameterNames != null ? parameterNames.length : 0); i++) {
                if (ArrayUtils.contains(idempotent.ignore(),parameterNames[i])) {
                    continue;
                }
                value.add(arguments[i]);
            }

            values.add(Arrays.hashCode(value.toArray()));

        } else {
            Arrays
                    .stream(idempotent.value())
                    .map(v -> valueGenerator.generate(v, method, arguments))
                    .forEach(values::add);
        }

        RBucket<List<Object>> bucket = redissonClient.getBucket(keyValue.toString());

        List<Object> existValues = redissonClient.getList(keyValue.toString()).get();

        if (CollectionUtils.isNotEmpty(existValues) && values.stream().anyMatch(existValues::contains)) {
            return true;
        }

        boolean result = bucket.trySet(
                values,
                idempotent.expirationTime().value(),
                idempotent.expirationTime().unit()
        );

        return !result;
    }

}
