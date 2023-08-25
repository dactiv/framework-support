package com.github.dactiv.framework.security.audit;

/**
 * 审计类型枚举，用于说明在系统审计方面使用什么审计实现类
 *
 * @author maurice.chen
 */
public enum AuditType {

    /**
     * 系统默认的内存管理实现 {@link org.springframework.boot.actuate.audit.InMemoryAuditEventRepository}
     */
    Memory,

    /**
     * 使用 Elasticsearch 作为存储的系统审计实现
     */
    Elasticsearch,

    /**
     * 使用 Mongo 作为存储的系统审计实现
     */
    Mongo
}
