package com.github.dactiv.framework.security;

import com.github.dactiv.framework.security.audit.AuditType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 审计配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("dactiv.authentication.audit")
public class AuditProperties {

    /**
     * 审计类型
     */
    private AuditType type = AuditType.Memory;

    public AuditProperties() {
    }

    /**
     * 获取审计类型
     *
     * @return 审计类型
     */
    public AuditType getType() {
        return type;
    }

    /**
     * 设置审计类型
     *
     * @param type 审计类型
     */
    public void setType(AuditType type) {
        this.type = type;
    }
}
