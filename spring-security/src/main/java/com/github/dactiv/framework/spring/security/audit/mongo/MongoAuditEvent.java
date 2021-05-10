package com.github.dactiv.framework.spring.security.audit.mongo;

import com.github.dactiv.framework.spring.security.audit.AuditEventEntity;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Mongo 审计事件实现
 *
 * @author maurice.chen
 */
public class MongoAuditEvent extends AuditEventEntity {

    private static final long serialVersionUID = -5131177712335189029L;

    /**
     * 主键 id
     */
    @Id
    private String id;

    /**
     * 审计类型
     */
    @Indexed
    private String type;

    /**
     * 创建时间
     */
    @Indexed
    private LocalDateTime creationTime = LocalDateTime.now();

    /**
     * 操作者
     */
    @Indexed
    private String principal;

    public MongoAuditEvent() {
    }

    public MongoAuditEvent(AuditEvent auditEvent) {
        super(auditEvent);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getPrincipal() {
        return principal;
    }

    @Override
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    @Override
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }
}
