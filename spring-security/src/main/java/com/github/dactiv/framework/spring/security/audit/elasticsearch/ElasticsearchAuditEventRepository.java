package com.github.dactiv.framework.spring.security.audit.elasticsearch;

import com.github.dactiv.framework.spring.security.audit.AuditEventEntity;
import com.github.dactiv.framework.spring.security.audit.DateIndexGenerator;
import com.github.dactiv.framework.spring.security.audit.IndexGenerator;
import com.github.dactiv.framework.spring.security.audit.PageAuditEventRepository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * es 审计事件仓库实现
 *
 * @author maurice.chen
 */
public class ElasticsearchAuditEventRepository implements PageAuditEventRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchAuditEventRepository.class);

    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    private SecurityProperties securityProperties;

    private IndexGenerator indexGenerator;

    public ElasticsearchAuditEventRepository() {
    }

    public ElasticsearchAuditEventRepository(ElasticsearchRestTemplate elasticsearchRestTemplate,
                                             SecurityProperties securityProperties) {
        this(elasticsearchRestTemplate, securityProperties, new DateIndexGenerator());
    }

    public ElasticsearchAuditEventRepository(ElasticsearchRestTemplate elasticsearchRestTemplate,
                                             SecurityProperties securityProperties,
                                             IndexGenerator indexGenerator) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.securityProperties = securityProperties;
        this.indexGenerator = indexGenerator;
    }

    @Override
    public void add(AuditEvent event) {

        String index = indexGenerator.generateIndex(event).toLowerCase();

        try {

            ElasticsearchAuditEvent auditEventEntity = new ElasticsearchAuditEvent(event);

            if (!auditEventEntity.getPrincipal().equals(securityProperties.getUser().getName())) {

                IndexQueryBuilder indexQueryBuilder = new IndexQueryBuilder()
                        .withId(auditEventEntity.getId())
                        .withObject(auditEventEntity);

                elasticsearchRestTemplate.index(indexQueryBuilder.build(), IndexCoordinates.of(index));
            }

        } catch (Exception e) {
            LOGGER.error("新增" + event.getPrincipal() + "审计事件失败", e);
        }

    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {

        String index = indexGenerator.getDefaultIndexPrefix() + "-*";

        Criteria criteria  = createCriteria(after, type);

        if (StringUtils.isNotEmpty(principal)) {
            criteria = criteria.and("principal").is(principal);
            index = indexGenerator.getDefaultIndexPrefix() + "-" + principal + "-*";
        }

        return elasticsearchRestTemplate
                .search(new CriteriaQuery(criteria), ElasticsearchAuditEvent.class, IndexCoordinates.of(index))
                .stream()
                .map(SearchHit::getContent)
                .map(AuditEventEntity::toAuditEvent)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AuditEvent> findPage(Pageable pageable, String principal, Instant after, String type) {

        String index = indexGenerator.getDefaultIndexPrefix() + "-*";

        Criteria criteria  = createCriteria(after, type);

        if (StringUtils.isNotEmpty(principal)) {
            criteria = criteria.and("principal").contains(principal);
            index = indexGenerator.getDefaultIndexPrefix() + "-" + principal + "-*";
        }

        List<AuditEvent> content = elasticsearchRestTemplate
                .search(new CriteriaQuery(criteria, pageable), AuditEventEntity.class, IndexCoordinates.of(index))
                .stream().map(SearchHit::getContent)
                .map(AuditEventEntity::toAuditEvent)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, content.size());
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
            criteria = criteria.and("type").greaterThanEqual(type);
        }

        return criteria;
    }

    /**
     * 设置索引生成器
     *
     * @param indexGenerator 索引生成器
     */
    public void setIndexGenerator(IndexGenerator indexGenerator) {
        this.indexGenerator = indexGenerator;
    }
}
