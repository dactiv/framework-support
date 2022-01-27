package com.github.dactiv.framework.security.audit;

import org.springframework.boot.actuate.audit.AuditEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 审计事件实体类
 *
 * @author maurice
 */
public class PluginAuditEvent extends AuditEvent {

    public static final String DEFAULT_INDEX_NAME = "audit-event";

    private static final long serialVersionUID = 8633684304971875621L;

    /**
     * 主键 id
     */
    private String id;

    public PluginAuditEvent(AuditEvent auditEvent) {
        super(auditEvent.getTimestamp(), auditEvent.getPrincipal(), auditEvent.getType(), auditEvent.getData());
        this.id = UUID.randomUUID().toString();
    }

    public PluginAuditEvent(String principal, String type, Map<String, Object> data) {
        super(principal, type, data);
        this.id = UUID.randomUUID().toString();
    }

    public PluginAuditEvent(String principal, String type, String... data) {
        super(principal, type, data);
        this.id = UUID.randomUUID().toString();
    }

    public PluginAuditEvent(Instant timestamp, String principal, String type, Map<String, Object> data) {
        super(timestamp, principal, type, data);
        this.id = UUID.randomUUID().toString();
    }

    /**
     * 获取主键 id
     *
     * @return 主键 id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置主键 id
     *
     * @param id 主键 id
     */
    public void setId(String id) {
        this.id = id;
    }


}
