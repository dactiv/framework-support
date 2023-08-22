package com.github.dactiv.framework.security.plugin;

import com.github.dactiv.framework.security.enumerate.ResourceType;

import java.lang.annotation.*;

/**
 * 插件信息注解
 *
 * @author maurice.chen
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {

    /**
     * 资源名称
     *
     * @return 名称
     */
    String name();

    /**
     * 资源 icon
     *
     * @return icon
     */
    String icon() default "";

    /**
     * 权限信息
     *
     * @return 权限信息
     */
    String[] authority() default "";

    /**
     * 唯一识别
     *
     * @return 值
     */
    String id() default "";

    /**
     * 父类识别
     *
     * @return 值
     */
    String parent() default "";

    /**
     * 类型，默认为 ResourceType.MENU
     *
     * @return 类型
     *
     * @see ResourceType
     */
    ResourceType type() default ResourceType.Security;

    /**
     * 是否审计
     *
     * @return true 是，否则 false
     */
    boolean audit() default false;

    /**
     * 来源
     *
     * @return 来源
     *
     */
    String[] sources() default {};

    /**
     * 顺序值，默认为 0
     *
     * @return 顺序之
     */
    int sort() default 0;

    /**
     * 备注
     *
     * @return 备注
     */
    String remark() default "";

    /**
     * 是否操作数据留痕
     *
     * @return true 是，否则 false
     */
    boolean operationDataTrace() default false;

}
