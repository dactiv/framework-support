package com.github.dactiv.framework.security.audit.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.page.TotalPage;
import com.github.dactiv.framework.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.security.audit.PluginAuditEventRepository;
import com.github.dactiv.framework.security.audit.elasticsearch.index.IndexGenerator;
import com.github.dactiv.framework.security.audit.elasticsearch.index.support.DateIndexGenerator;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * es 审计事件仓库实现
 *
 * @author maurice.chen
 */
public class ElasticsearchAuditEventRepository implements PluginAuditEventRepository {

    public static final int DEFAULT_FIND_SIZE = 10000;

    public static final String MAPPING_FILE_PATH = "elasticsearch/plugin-audit-mapping.json";

    public static final String DEFAULT_INDEX_NAME = "ix_http_request_audit_event";

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchAuditEventRepository.class);

    private final ElasticsearchTemplate elasticsearchTemplate;

    private final List<String> ignorePrincipals;

    private final IndexGenerator indexGenerator;

    public ElasticsearchAuditEventRepository(ElasticsearchTemplate elasticsearchTemplate,
                                             String indexName,
                                             List<String> ignorePrincipals) {

        this.elasticsearchTemplate = elasticsearchTemplate;
        this.ignorePrincipals = ignorePrincipals;

        this.indexGenerator = new DateIndexGenerator(
                indexName,
                Casts.UNDERSCORE,
                List.of(RestResult.DEFAULT_TIMESTAMP_NAME, NumberIdEntity.CREATION_TIME_FIELD_NAME)
        );
    }

    @Override
    public void add(AuditEvent event) {

        PluginAuditEvent pluginAuditEvent = new PluginAuditEvent(
                event.getPrincipal(),
                event.getType(),
                event.getData()
        );

        if (ignorePrincipals.contains(pluginAuditEvent.getPrincipal())) {
            return ;
        }

        if (PluginAuditEvent.class.isAssignableFrom(event.getClass())) {
            pluginAuditEvent = Casts.cast(event);
        }

        try {

            String index = indexGenerator.generateIndex(pluginAuditEvent).toLowerCase();

            IndexCoordinates indexCoordinates = IndexCoordinates.of(index);
            IndexOperations indexOperations = elasticsearchTemplate.indexOps(indexCoordinates);
            createIndexIfNotExists(indexOperations, MAPPING_FILE_PATH);

            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withId(pluginAuditEvent.getId())
                    .withObject(pluginAuditEvent)
                    .build();

            elasticsearchTemplate.index(indexQuery, indexCoordinates);
        } catch (Exception e) {
            LOGGER.warn("新增 elasticsearch" + event.getPrincipal() + " 审计事件出现异常", e);
        }

    }

    public static void createIndexIfNotExists(IndexOperations indexOperations, String mappingFilePath) throws IOException {
        if (indexOperations.exists()) {
            return ;
        }

        indexOperations.create();
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(mappingFilePath)) {
            Map<String, Object> mapping = Casts.readValue(input, new TypeReference<>() {});
            indexOperations.putMapping(Document.from(mapping));
        }
    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {

        Assert.notNull(after, "查询 elasticsearch 审计数据时 after 参数不能为空");

        String index = getIndexName(after).toLowerCase();

        NativeQueryBuilder builder = new NativeQueryBuilder()
                .withPageable(org.springframework.data.domain.PageRequest.ofSize(DEFAULT_FIND_SIZE))
                .withQuery(q -> createQueryBuilder(q, after, type, principal))
                .withSort(Sort.by(Sort.Order.desc(RestResult.DEFAULT_TIMESTAMP_NAME)));

        List<AuditEvent> result = new LinkedList<>();

        try {
            SearchHits<Map<String, Object>> hits = Casts.cast(elasticsearchTemplate.search(builder.build(), Map.class, IndexCoordinates.of(index)));
            result = hits.stream().map(s -> this.createAuditEvent(s.getContent())).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.warn("查询索引 [" + index + "] 数据出现错误", e);
        }

        return result;
    }

    @Override
    public Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type) {

        Assert.notNull(after, "查询 elasticsearch 审计数据分页时 after 参数不能为空");

        String index = getIndexName(after).toLowerCase();

        NativeQueryBuilder builder = new NativeQueryBuilder()
                .withQuery(q -> createQueryBuilder(q, after, type, principal))
                .withSort(Sort.by(Sort.Order.desc(RestResult.DEFAULT_TIMESTAMP_NAME)))
                .withPageable(org.springframework.data.domain.PageRequest.of(pageRequest.getNumber() - 1, pageRequest.getSize()));

        try {
            SearchHits<Map<String, Object>> hits = Casts.cast(elasticsearchTemplate.search(builder.build(), Map.class, IndexCoordinates.of(index)));
            List<AuditEvent> content = hits.stream().map(s -> this.createAuditEvent(s.getContent())).collect(Collectors.toList());
            return new TotalPage<>(pageRequest, content, hits.getTotalHits());
        } catch (Exception e) {
            LOGGER.warn("查询索引 [" + index + "] 分页数据出现错误", e);
        }

        return new TotalPage<>(pageRequest, new LinkedList<>(), 0);
    }

    @Override
    public AuditEvent createAuditEvent(Map<String, Object> map) {
        PluginAuditEvent pluginAuditEvent = Casts.cast(PluginAuditEventRepository.super.createAuditEvent(map));
        pluginAuditEvent.setId(map.get(IdEntity.ID_FIELD_NAME).toString());
        return pluginAuditEvent;
    }

    @Override
    public AuditEvent get(StringIdEntity idEntity) {

        String index = indexGenerator.generateIndex(idEntity).toLowerCase();
        try {
            //noinspection unchecked
            Map<String, Object> map = elasticsearchTemplate.get(idEntity.getId(), Map.class, IndexCoordinates.of(index));
            if (MapUtils.isNotEmpty(map)) {
                return createAuditEvent(map);
            }
        } catch (Exception e) {
            LOGGER.warn("通过 ID 查询索引 [" + index + "] 出现错误", e);
        }

        return null;
    }

    public String getIndexName(Instant instant) {
        StringIdEntity id = new StringIdEntity();
        id.setCreationTime(java.sql.Date.from(instant));
        return indexGenerator.generateIndex(id).toLowerCase();
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
    private ObjectBuilder<Query> createQueryBuilder(Query.Builder builder, Instant after, String type, String principal) {
        List<Query> queryList = new LinkedList<>();

        if (StringUtils.isNotBlank(type)) {
            queryList.add(Query.of(q -> q.term(t -> t.field(PluginAuditEvent.TYPE_FIELD_NAME).value(type))));
        }

        if (Objects.nonNull(after)) {
            queryList.add(Query.of(q -> q.range(r -> r.field(RestResult.DEFAULT_TIMESTAMP_NAME).gte(JsonData.of(after.getEpochSecond())))));
        }

        if (StringUtils.isNotBlank(principal)) {
            queryList.add(Query.of(q -> q.term(t -> t.field(PluginAuditEvent.PRINCIPAL_FIELD_NAME).value(principal))));
        }

        return builder.bool(t -> t.must(queryList));
    }

    public ElasticsearchOperations getElasticsearchTemplate() {
        return elasticsearchTemplate;
    }
}
