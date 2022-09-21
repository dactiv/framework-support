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

    public static final String PRINCIPAL_FIELD_NAME = "principal";

    public static final String PRINCIPAL_TYPE_FIELD_NAME = "principal_type";

    public static final String PRINCIPAL_ID_FIELD_NAME = "principal_id";

    public static final String META_FIELD_NAME = "meta";

    public static final String TYPE_FIELD_NAME = "type";

    private static final long serialVersionUID = 8633684304971875621L;

    /**
     * 主键 id
     */
    private String id;

    private Map<String, Object> meta;

    private String principalId;

    private String principalType;

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

    /**
     * 获取元数据信息
     *
     * @return 元数据信息
     */
    public Map<String, Object> getMeta() {
        return meta;
    }

    /**
     * 设置元数据信息
     *
     * @param meta 元数据信息
     */
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    /**
     * 获取当事人 id
     *
     * @return 当事人 id
     */
    public String getPrincipalId() {
        return principalId;
    }

    /**
     * 设置当事人 id
     *
     * @param principalId 当事人 id
     */
    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    /**
     * 获取当事人类型
     *
     * @return 当事人类型
     */
    public String getPrincipalType() {
        return principalType;
    }

    /**
     * 设置当事人类型
     *
     * @param principalType 当事人类型
     */
    public void setPrincipalType(String principalType) {
        this.principalType = principalType;
    }
}
