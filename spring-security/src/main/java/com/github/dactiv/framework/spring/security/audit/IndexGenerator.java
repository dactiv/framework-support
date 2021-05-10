package com.github.dactiv.framework.spring.security.audit;

import org.springframework.boot.actuate.audit.AuditEvent;

/**
 * 索引生成器
 *
 * @author maurice.chen
 */
public interface IndexGenerator {

    /**
     * 生成索引
     *
     * @param auditEvent 审计事件
     *
     * @return 索引值
     */
    String generateIndex(AuditEvent auditEvent);

    /**
     * 获取默认索引前缀
     *
     * @return 默认索引前缀
     */
    String getDefaultIndexPrefix();
}
