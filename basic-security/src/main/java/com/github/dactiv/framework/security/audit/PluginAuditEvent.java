package com.github.dactiv.framework.security.audit;

import org.springframework.boot.actuate.audit.AuditEvent;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 审计事件实体类
 *
 * @author maurice
 */
public class PluginAuditEvent extends AuditEvent {

    @Serial
    private static final long serialVersionUID = 8633684304971875621L;

    public static final String PRINCIPAL_FIELD_NAME = "principal";

    public static final String PRINCIPAL_META_FIELD_NAME = "principalMeta";

    public static final String TYPE_FIELD_NAME = "type";

    /**
     * 主键 id
     */
    private String id;

    private Map<String, Object> principalMeta = new LinkedHashMap<>();

    /**
     * 操作数据留痕 ID
     */
    private String traceId;

    public PluginAuditEvent(AuditEvent auditEvent) {
        super(auditEvent.getTimestamp(), auditEvent.getPrincipal(), auditEvent.getType(), auditEvent.getData());
        this.id = UUID.randomUUID().toString();
    }

    public PluginAuditEvent(String principal, String type, Map<String, Object> data) {
        super(Instant.now(), principal, type, data);
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

    /**
     * 获取当事人元数据
     *
     * @return 当事人元数据
     */
    public Map<String, Object> getPrincipalMeta() {
        return principalMeta;
    }

    /**
     * 设置当事人元数据
     *
     * @param principalMeta 当事人元数据
     */
    public void setPrincipalMeta(Map<String, Object> principalMeta) {
        this.principalMeta = principalMeta;
    }

    /**
     * 获取操作数据留痕 ID
     *
     * @return 操作数据留痕 ID
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置操作数据留痕 ID
     *
     * @param traceId 操作数据留痕 ID
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
