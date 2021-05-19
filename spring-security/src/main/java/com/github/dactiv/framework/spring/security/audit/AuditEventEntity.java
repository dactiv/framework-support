package com.github.dactiv.framework.spring.security.audit;

import org.springframework.boot.actuate.audit.AuditEvent;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/**
 * 审计事件实体类
 *
 * @author maurice
 */
public class AuditEventEntity implements Serializable {

    public static final String DEFAULT_INDEX_NAME = "audit-event";

    private static final long serialVersionUID = 8633684304971875621L;

    /**
     * 主键 id
     */
    private String id;

    /**
     * 创建时间
     */
    private LocalDateTime creationTime = LocalDateTime.now();

    /**
     * 操作者
     */
    private String principal;

    /**
     * 审计类型
     */
    private String type;

    /**
     * 审计数据
     */
    private Map<String, Object> data;

    /**
     * 创建一个审计事件实体
     */
    public AuditEventEntity() {
        super();
    }

    /**
     * 创建一个审计事件实体
     *
     * @param auditEvent 审计事件
     */
    public AuditEventEntity(AuditEvent auditEvent) {
        this.principal = auditEvent.getPrincipal();
        this.type = auditEvent.getType();
        this.data = auditEvent.getData();
        this.setId(UUID.randomUUID().toString());
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
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * 设置创建时间
     *
     * @param creationTime 创建时间
     */
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * 获取当事人
     *
     * @return 当事人
     */
    public String getPrincipal() {
        return principal;
    }

    /**
     * 获取审计类型
     *
     * @return 审计类型
     */
    public String getType() {
        return type;
    }

    /**
     * 获取审计数据
     *
     * @return 审计数据
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * 设置当事人
     *
     * @param principal 当事人
     */
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /**
     * 设置审计类型
     *
     * @param type 审计类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 设置审计数据
     *
     * @param data 审计数据
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * 转换为 spring 默认的审计事件
     *
     * @return spring 默认审计事件
     */
    public AuditEvent toAuditEvent() {
        return new AuditEvent(creationTime.atZone(ZoneId.systemDefault()).toInstant(), principal, type, data);
    }
}
