package com.github.dactiv.framework.spring.security.audit;

import com.github.dactiv.framework.spring.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 审计事件实体类
 *
 * @author maurice
 */
@Document(indexName = "audit-event", type = ElasticsearchAuditEventRepository.DEFAULT_ES_TYPE_VALUE)
public class AuditEventEntity implements Serializable {

    /**
     * 主键 id
     */
    private String id;

    /**
     * 创建时间
     */
    private LocalDateTime creationTIme = LocalDateTime.now();

    /**
     * 操作者
     */
    private String principal;

    /**
     * 审计类型
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String type;

    /**
     * 审计数据
     */
    private Map<String, Object> data;

    /**
     * 创建一个审计事件实体
     */
    public AuditEventEntity() {
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
    public LocalDateTime getCreationTIme() {
        return creationTIme;
    }

    /**
     * 设置创建时间
     *
     * @param creationTIme 创建时间
     */
    public void setCreationTIme(LocalDateTime creationTIme) {
        this.creationTIme = creationTIme;
    }

    /**
     * 获取当事人
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
     * @param data
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
