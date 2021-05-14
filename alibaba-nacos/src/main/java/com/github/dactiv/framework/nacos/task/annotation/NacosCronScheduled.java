package com.github.dactiv.framework.nacos.task.annotation;

import java.lang.annotation.*;

/**
 * nacos 任务调度
 *
 * @author maurice.chen
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface NacosCronScheduled {

    /**
     * cron 表达式
     *
     * @return 表达式
     */
    String cron();

    /**
     * 任务名称
     *
     * @return 名称
     */
    String name() default "";

    /**
     * 时区
     *
     * @return 时区
     */
    String zone() default "";
}
