package com.github.dactiv.framework.security.audit.elasticsearch;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.security.audit.PluginAuditEventRepository;
import com.github.dactiv.framework.security.audit.elasticsearch.index.IndexGenerator;
import com.github.dactiv.framework.security.audit.elasticsearch.index.support.DateIndexGenerator;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

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

        this.indexGenerator = new DateIndexGenerator(
                PluginAuditEvent.DEFAULT_INDEX_NAME,
                "-",
                RestResult.DEFAULT_TIMESTAMP_NAME
        );
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

        QueryBuilder queryBuilder = createQueryBuilder(after, type, principal);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSort(SortBuilders.fieldSort("timestamp").order(SortOrder.DESC));

        return elasticsearchRestTemplate
                .search(builder.build(), Map.class, IndexCoordinates.of(index))
                .stream()
                .map(SearchHit::getContent)
                .map(this::createPluginAuditEvent)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type) {

        String index = getIndex(after, principal, type);

        QueryBuilder queryBuilder = createQueryBuilder(after, type, principal);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSort(SortBuilders.fieldSort("timestamp").order(SortOrder.DESC))
                .withPageable(org.springframework.data.domain.PageRequest.of(pageRequest.getNumber() - 1, pageRequest.getSize()));

        List<PluginAuditEvent> content = elasticsearchRestTemplate
                .search(builder.build(), Map.class, IndexCoordinates.of(index))
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
     *
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
     *
     * @return 查询条件
     */
    private QueryBuilder createQueryBuilder(Instant after, String type, String principal) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        if (StringUtils.isNotBlank(type)) {
            queryBuilder = queryBuilder.must(QueryBuilders.termQuery("type", type));
        }

        if (Objects.nonNull(after)) {
            queryBuilder = queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(after.getEpochSecond()));
        }

        if (StringUtils.isNotBlank(principal)) {
            queryBuilder = queryBuilder.must(QueryBuilders.termQuery("principal", principal));
        }

        return queryBuilder;
    }

    /**
     * 获取当前 index
     *
     * @param after     在什么时间之后
     * @param type      类型
     * @param principal 操作人
     *
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
