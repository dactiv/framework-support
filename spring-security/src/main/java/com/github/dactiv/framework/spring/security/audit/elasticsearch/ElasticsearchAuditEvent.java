package com.github.dactiv.framework.spring.security.audit.elasticsearch;

import com.github.dactiv.framework.spring.security.audit.AuditEventEntity;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Elasticsearch 审计事件实现
 *
 * @author maurice.chen
 */
public class ElasticsearchAuditEvent extends AuditEventEntity {

    private static final long serialVersionUID = -2472369877551478898L;

    public ElasticsearchAuditEvent() {
    }

    public ElasticsearchAuditEvent(AuditEvent auditEvent) {
        super(auditEvent);
    }

    /**
     * 审计类型
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String type;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
