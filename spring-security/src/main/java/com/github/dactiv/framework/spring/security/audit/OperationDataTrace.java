package com.github.dactiv.framework.spring.security.audit;

import java.lang.annotation.*;

/**
 * 数据操作留痕注解
 *
 * @author maurice.chen
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationDataTrace {
}
