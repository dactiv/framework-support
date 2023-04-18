package com.github.dactiv.framework.spring.security.audit;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.page.TotalPage;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.framework.mybatis.plus.audit.EntityIdOperationDataTraceRecord;
import com.github.dactiv.framework.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import com.github.dactiv.framework.security.audit.elasticsearch.index.IndexGenerator;
import com.github.dactiv.framework.security.audit.elasticsearch.index.support.DateIndexGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.util.Assert;
import org.springframework.validation.DataBinder;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Elasticsearch 存储的操作数据留痕仓库实现
 *
 * @author maurice.chen
 */
public class ElasticsearchOperationDataTraceRepository extends UserDetailsOperationDataTraceRepository {

    public static final String DEFAULT_INDEX_NAME = "ix_user_operation_data_trace";

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchAuditEventRepository.class);

    private final ElasticsearchOperations elasticsearchOperations;

    private final IndexGenerator indexGenerator;

    public ElasticsearchOperationDataTraceRepository(List<String> ignorePrincipals,
                                                     String indexName,
                                                     ElasticsearchOperations elasticsearchOperations) {
        super(ignorePrincipals);
        this.elasticsearchOperations = elasticsearchOperations;
        this.indexGenerator = new DateIndexGenerator(
                indexName,
                Casts.UNDERSCORE,
                List.of(RestResult.DEFAULT_TIMESTAMP_NAME, NumberIdEntity.CREATION_TIME_FIELD_NAME)
        );
    }

    @Override
    public void saveOperationDataTraceRecord(List<OperationDataTraceRecord> records) {
        try {
            for (OperationDataTraceRecord record : records) {
                String index = indexGenerator.generateIndex(record).toLowerCase();
                elasticsearchOperations.save(record, IndexCoordinates.of(index));
            }
        } catch (Exception e) {
            LOGGER.warn("新增 elasticsearch 操作数据留痕出现异常", e);
        }
    }

    @Override
    public List<OperationDataTraceRecord> find(String target) {
        throw new UnsupportedOperationException("不支持 find(String target) 操作");
    }

    @Override
    public Page<OperationDataTraceRecord> findPage(PageRequest pageRequest, String target) {
        throw new UnsupportedOperationException("不支持 findPage(PageRequest pageRequest, String target) 操作");
    }

    @Override
    public List<OperationDataTraceRecord> find(String target, Object entityId) {
        throw new UnsupportedOperationException("不支持 find(String target, Object entityId) 操作");
    }

    @Override
    public Page<OperationDataTraceRecord> findPage(PageRequest pageRequest, String target, Object entityId) {
        throw new UnsupportedOperationException("不支持 findPage(PageRequest pageRequest, String target, Object entityId) 操作");
    }

    @Override
    public List<OperationDataTraceRecord> find(String target, Date creationTime, Object entityId) {
        Assert.notNull(creationTime, "查询 elasticsearch 操作留痕数据时 creationTime 参数不能为空");

        String index = getIndexName(creationTime).toLowerCase();

        NativeQueryBuilder builder = new NativeQueryBuilder()
                .withQuery(q -> createQueryBuilder(q, creationTime, target, entityId))
                .withSort(Sort.by(Sort.Order.desc(NumberIdEntity.CREATION_TIME_FIELD_NAME)));

        List<OperationDataTraceRecord> result = new LinkedList<>();

        try {
            SearchHits<OperationDataTraceRecord> hits = Casts.cast(elasticsearchOperations.search(builder.build(), OperationDataTraceRecord.class, IndexCoordinates.of(index)));
            result = hits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.warn("查询索引 [" + index + "] 数据出现错误", e);
        }

        return result;
    }

    @Override
    public Page<OperationDataTraceRecord> findPage(PageRequest pageRequest, Date creationTime, String target, Object entityId) {
        Assert.notNull(creationTime, "查询 elasticsearch 操作留痕数据分页时 creationTime 参数不能为空");

        String index = getIndexName(creationTime).toLowerCase();

        NativeQueryBuilder builder = new NativeQueryBuilder()
                .withQuery(q -> createQueryBuilder(q, creationTime, target, entityId))
                .withSort(Sort.by(Sort.Order.desc(NumberIdEntity.CREATION_TIME_FIELD_NAME)))
                .withPageable(org.springframework.data.domain.PageRequest.of(pageRequest.getNumber() - 1, pageRequest.getSize()));

        try {
            SearchHits<OperationDataTraceRecord> hits = Casts.cast(elasticsearchOperations.search(builder.build(), OperationDataTraceRecord.class, IndexCoordinates.of(index)));
            List<OperationDataTraceRecord> result = hits.stream().map(SearchHit::getContent).collect(Collectors.toList());
            return new TotalPage<>(pageRequest, result, hits.getTotalHits());
        } catch (Exception e) {
            LOGGER.warn("查询索引 [" + index + "] 数据出现错误", e);
        }

        return new TotalPage<>(pageRequest, new LinkedList<>(), 0);
    }

    @Override
    public OperationDataTraceRecord get(StringIdEntity idEntity) {

        String index = indexGenerator.generateIndex(idEntity).toLowerCase();
        try {
            return elasticsearchOperations.get(idEntity.getId(), OperationDataTraceRecord.class, IndexCoordinates.of(index));
        } catch (Exception e) {
            LOGGER.warn("通过 ID 查询索引 [" + index + "] 出现错误", e);
        }

        return null;
    }


    /**
     * 创建查询条件
     *
     * @param creationTime 在什么时间之后的
     * @param target       目标值
     * @param entityId     实体 id
     * @return 查询条件
     */
    private ObjectBuilder<Query> createQueryBuilder(Query.Builder builder, Date creationTime, String target, Object entityId) {

        List<Query> queryList = new LinkedList<>();

        if (StringUtils.isNotBlank(target)) {
            queryList.add(Query.of(q -> q.term(t -> t.field(DataBinder.DEFAULT_OBJECT_NAME).value(target))));
        }

        if (Objects.nonNull(creationTime)) {
            queryList.add(Query.of(q -> q.range(r -> r.field(NumberIdEntity.CREATION_TIME_FIELD_NAME).gte(JsonData.of(creationTime.getTime())))));
        }

        if (Objects.nonNull(entityId)) {
            queryList.add(Query.of(q -> q.term(t -> t.field(EntityIdOperationDataTraceRecord.ENTITY_ID_FIELD_NAME).value(entityId.toString()))));
        }

        return builder.bool(t -> t.must(queryList));
    }

    public String getIndexName(Date creationTime) {
        StringIdEntity id = new StringIdEntity();
        id.setCreationTime(creationTime);
        return indexGenerator.generateIndex(id).toLowerCase();
    }
}
