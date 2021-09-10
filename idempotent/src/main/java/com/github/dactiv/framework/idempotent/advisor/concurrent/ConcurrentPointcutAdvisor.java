package com.github.dactiv.framework.idempotent.advisor.concurrent;

import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

/**
 * 并发处理的切面实现
 *
 * @author maurice
 */
public class ConcurrentPointcutAdvisor extends AbstractPointcutAdvisor {

    private static final long serialVersionUID = -2797648387592489604L;

    private final ConcurrentInterceptor concurrentInterceptor;

    public ConcurrentPointcutAdvisor(ConcurrentInterceptor concurrentInterceptor) {
        this.concurrentInterceptor = concurrentInterceptor;
    }

    @Override
    public Pointcut getPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return method.isAnnotationPresent(Concurrent.class);
            }

        };
    }

    @Override
    public Advice getAdvice() {
        return concurrentInterceptor;
    }
}
