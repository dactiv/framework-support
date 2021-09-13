package com.github.dactiv.framework.idempotent.annotation;

import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.idempotent.advisor.LockType;

import java.lang.annotation.*;

/**
 * 并发处理注解
 *
 * @author maurice
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Concurrent {

    /**
     * 并发的 key
     * <p>spring el 规范: 启用 spring el 时，通过中括号([])识别启用</p>
     * <p>如:</p>
     * <p>@Concurrent(value="[#vo.fieldName]")</p>
     * <p>public void save(Vo vo);</p>
     * @return key 名称
     */
    String value() default "";

    /**
     * 异常信息
     *
     * @return 信息
     */
    String exception() default "请不要重复操作";

    /**
     * 锁等待时间
     *
     * @return 锁等待时间
     */
    long waitTime() default -1;

    /**
     * 锁生存/释放时间
     *
     * @return 锁生存/释放时间
     */
    Time leaseTime() default @Time(1000);

    /**
     * 锁类型
     *
     * @return 所类型
     */
    LockType type() default LockType.FairLock;

}
