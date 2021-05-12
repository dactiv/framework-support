package com.github.dactiv.framework.spring.security.audit.mongo;

import com.github.dactiv.framework.spring.security.audit.AuditEventEntity;
import com.github.dactiv.framework.spring.security.audit.PageAuditEventRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * mongo 审计事件仓库实现
 *
 * @author maurice.chen
 */
public class MongoAuditEventRepository implements PageAuditEventRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoAuditEventRepository.class);

    private MongoTemplate mongoTemplate;

    private SecurityProperties securityProperties;

    public MongoAuditEventRepository() {
    }

    public MongoAuditEventRepository(MongoTemplate mongoTemplate,
                                     SecurityProperties securityProperties) {

        this.mongoTemplate = mongoTemplate;
        this.securityProperties = securityProperties;
    }

    @Override
    public void add(AuditEvent event) {

        AuditEventEntity auditEventEntity = new AuditEventEntity(event);

        try {

            if (!auditEventEntity.getPrincipal().equals(securityProperties.getUser().getName())) {
                mongoTemplate.save(auditEventEntity, AuditEventEntity.DEFAULT_INDEX_NAME);
            }

        } catch (Exception e) {
            LOGGER.error("新增" + event.getPrincipal() + "审计事件失败", e);
        }

    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {

        Criteria criteria = createCriteria(after, type);

        return mongoTemplate
                .find(new Query(criteria), AuditEventEntity.class, AuditEventEntity.DEFAULT_INDEX_NAME)
                .stream()
                .map(AuditEventEntity::toAuditEvent)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AuditEvent> findPage(Pageable pageable, String principal, Instant after, String type) {

        Criteria criteria = createCriteria(after, type);

        List<AuditEvent> data = mongoTemplate
                .find(new Query(criteria).with(pageable).with(pageable.getSort()), AuditEventEntity.class, AuditEventEntity.DEFAULT_INDEX_NAME)
                .stream()
                .map(AuditEventEntity::toAuditEvent)
                .collect(Collectors.toList());

        return new PageImpl<>(data, pageable, data.size());
    }

    /**
     * 创建查询条件
     *
     * @param after 在什么时间之后的
     * @param type 类型
     *
     * @return 查询条件
     */
    private Criteria createCriteria(Instant after, String type) {

        Criteria criteria = new Criteria();

        if (StringUtils.isNotEmpty(type)) {
            criteria = criteria.and("type").is(type);
        }

        if (after != null) {
            criteria = criteria.and("type").gte(type);
        }

        return criteria;
    }
}
