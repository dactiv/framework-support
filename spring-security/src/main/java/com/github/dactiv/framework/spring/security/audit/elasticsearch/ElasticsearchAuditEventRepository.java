package com.github.dactiv.framework.spring.security.audit.elasticsearch;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.security.audit.PluginAuditEventRepository;
import com.github.dactiv.framework.spring.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.spring.security.audit.StringIdEntity;
import com.github.dactiv.framework.spring.security.audit.elasticsearch.index.IndexGenerator;
import com.github.dactiv.framework.spring.security.audit.elasticsearch.index.support.DateIndexGenerator;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * es 审计事件仓库实现
 *
 * @author maurice.chen
 */
public class ElasticsearchAuditEventRepository implements PluginAuditEventRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchAuditEventRepository.class);

    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    private SecurityProperties securityProperties;

    private IndexGenerator indexGenerator;

    public ElasticsearchAuditEventRepository() {
    }

    public ElasticsearchAuditEventRepository(ElasticsearchRestTemplate elasticsearchRestTemplate,
                                             SecurityProperties securityProperties) {

        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.securityProperties = securityProperties;

        this.indexGenerator = new DateIndexGenerator(PluginAuditEvent.DEFAULT_INDEX_NAME, "-", "timestamp");
    }

    @Override
    public void add(AuditEvent event) {

        PluginAuditEvent pluginAuditEvent = new PluginAuditEvent(event);

        if (pluginAuditEvent.getPrincipal().equals(securityProperties.getUser().getName())) {
            return;
        }

        try {

            String index = indexGenerator.generateIndex(pluginAuditEvent).toLowerCase();

            IndexQueryBuilder indexQueryBuilder = new IndexQueryBuilder()
                    .withId(pluginAuditEvent.getId())
                    .withObject(pluginAuditEvent);

            elasticsearchRestTemplate.index(indexQueryBuilder.build(), IndexCoordinates.of(index));

        } catch (Exception e) {
            LOGGER.error("新增" + event.getPrincipal() + "审计事件失败", e);
        }

    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {

        String index = getIndex(after, principal, type);

        Criteria criteria = createCriteria(after, type, principal);

        Query query = new CriteriaQuery(criteria).addSort(Sort.by(Sort.Order.desc("timestamp")));

        return elasticsearchRestTemplate
                .search(query, Map.class, IndexCoordinates.of(index))
                .stream()
                .map(SearchHit::getContent)
                .map(this::createPluginAuditEvent)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type) {

        String index = getIndex(after, principal, type);

        Criteria criteria = createCriteria(after, type, principal);

        Query query = new CriteriaQuery(
                criteria,
                org.springframework.data.domain.PageRequest.of(pageRequest.getNumber() - 1, pageRequest.getSize())
        );

        query.addSort(Sort.by(Sort.Order.desc("timestamp")));

        List<PluginAuditEvent> content = elasticsearchRestTemplate
                .search(query, Map.class, IndexCoordinates.of(index))
                .stream()
                .map(SearchHit::getContent)
                .map(this::createPluginAuditEvent)
                .collect(Collectors.toList());

        return new Page<>(pageRequest, new ArrayList<>(content));
    }

    @Override
    public AuditEvent get(Object target) {

        if (!StringIdEntity.class.isAssignableFrom(target.getClass())) {
            throw new SystemException("目标对象不是 StringIdEntity 对象");
        }

        StringIdEntity stringIdEntity = Casts.cast(target);

        //noinspection unchecked
        Map<String, Object> map = elasticsearchRestTemplate.get(
                stringIdEntity.getId(),
                Map.class,
                IndexCoordinates.of(indexGenerator.generateIndex(stringIdEntity))
        );

        if (MapUtils.isNotEmpty(map)) {
            return createPluginAuditEvent(map);
        }

        return null;
    }

    /**
     * 创建插件审计事件
     *
     * @param map map 数据源
     * @return 插件审计事件
     */
    public PluginAuditEvent createPluginAuditEvent(Map<String, Object> map) {
        AuditEvent auditEvent = createAuditEvent(map);

        PluginAuditEvent pluginAuditEvent = new PluginAuditEvent(auditEvent);
        pluginAuditEvent.setId(map.get("id").toString());

        return pluginAuditEvent;
    }

    /**
     * 创建查询条件
     *
     * @param after     在什么时间之后的
     * @param type      类型
     * @param principal 操作人
     * @return 查询条件
     */
    private Criteria createCriteria(Instant after, String type, String principal) {

        Criteria criteria = new Criteria();

        if (StringUtils.isNotEmpty(type)) {
            criteria = criteria.and("type").is(type);
        }

        if (Objects.nonNull(after)) {
            criteria = criteria.and("timestamp").greaterThanEqual(after.getEpochSecond());
        }

        if (StringUtils.isNotEmpty(principal)) {
            criteria = criteria.and("principal").contains(principal);
        }

        return criteria;
    }

    /**
     * 获取当前 index
     *
     * @param after     在什么时间之后
     * @param type      类型
     * @param principal 操作人
     * @return index 信息
     */
    private String getIndex(Instant after, String type, String principal) {
        try {

            AuditEvent auditEvent = new AuditEvent(
                    after,
                    StringUtils.defaultString(principal, ""),
                    StringUtils.defaultString(type, ""),
                    new LinkedHashMap<>()
            );

            return indexGenerator.generateIndex(auditEvent).toLowerCase();

        } catch (Exception e) {
            return PluginAuditEvent.DEFAULT_INDEX_NAME + "-*";
        }
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
