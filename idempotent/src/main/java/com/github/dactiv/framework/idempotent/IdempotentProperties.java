package com.github.dactiv.framework.idempotent;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等配置类
 *
 * @author maurice.chen
 */
@ConfigurationProperties("dactiv.idempotent")
public class IdempotentProperties {

    /**
     * 幂等注解启用类型
     */
    private IdempotentType type;

    public IdempotentProperties() {
    }

    /**
     * 获取幂等注解启用类型
     *
     * @return 幂等注解启用类型
     */
    public IdempotentType getType() {
        return type;
    }

    /**
     * 设置幂等注解启用类型
     *
     * @param type 幂等注解启用类型
     */
    public void setType(IdempotentType type) {
        this.type = type;
    }
}
