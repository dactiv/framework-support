package com.github.dactiv.framework.mybatis.plus.audit;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.page.TotalPage;
import com.github.dactiv.framework.mybatis.enumerate.OperationDataType;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.framework.mybatis.interceptor.audit.support.InMemoryOperationDataTraceRepository;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlException;

import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * mybatis-plus 操作数据留痕仓库实现
 *
 * @author maurice.chen
 */
public class MybatisPlusOperationDataTraceRepository extends InMemoryOperationDataTraceRepository implements EntityIdOperationDataTraceRepository{

    public static final String WHERE_SEPARATE = "\\s+(?i:and|or)\\s+";

    @Override
    protected List<OperationDataTraceRecord> createInsertRecord(Insert insert, MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception {

        if (parameter instanceof MapperMethod.ParamMap<?> map) {
            Object entity = map.get(Constants.ENTITY);

            if (Objects.isNull(entity)) {
                return super.createInsertRecord(insert, mappedStatement, statement, parameter);
            }
            if (!BasicIdentification.class.isAssignableFrom(entity.getClass())) {
                return super.createInsertRecord(insert, mappedStatement, statement, parameter);
            }

            BasicIdentification<Object> basicIdentification = Casts.cast(entity);
            OperationDataTraceRecord record = createEntityIdOperationDataTraceRecord(
                    basicIdentification,
                    insert.getTable().getName(),
                    OperationDataType.INSERT
            );
            return List.of(record);
        } else if (BasicIdentification.class.isAssignableFrom(parameter.getClass())) {
            BasicIdentification<Object> basicIdentification = Casts.cast(parameter);
            OperationDataTraceRecord record = createEntityIdOperationDataTraceRecord(
                    basicIdentification,
                    insert.getTable().getName(),
                    OperationDataType.INSERT
            );
            return List.of(record);
        }

        return super.createInsertRecord(insert, mappedStatement, statement, parameter);
    }

    private EntityIdOperationDataTraceRecord createEntityIdOperationDataTraceRecord(BasicIdentification<Object> basicIdentification, String tableName, OperationDataType type) throws UnknownHostException {
        OperationDataTraceRecord result = super.createBasicOperationDataTraceRecord(
                type,
                tableName,
                new LinkedHashMap<>()
        );
        EntityIdOperationDataTraceRecord entityRecord = Casts.of(result, EntityIdOperationDataTraceRecord.class);
        entityRecord.setSubmitData(Casts.convertValue(basicIdentification, new TypeReference<>() {}));
        entityRecord.setEntityId(basicIdentification.getId());
        return entityRecord;
    }

    @Override
    protected List<OperationDataTraceRecord> createUpdateRecord(Update update, MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception {
        return createUpdateOrDeleteRecord(update.getTable().getName(), OperationDataType.UPDATE, parameter);
    }

    private Map<String, Object> getUpdateModifiedMap(String slqSet, Object parameterObject) throws OgnlException {
        String[] fields = StringUtils.splitByWholeSeparator(slqSet, Casts.COMMA);
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : fields) {
            String name = StringUtils.substringBefore(field, Casts.EQ);
            String exp = StringUtils.substringAfter(field, Casts.EQ);
            Object value = getOgnlValue(exp, parameterObject);
            result.put(name, value);
        }

        return Collections.unmodifiableMap(result);
    }

    private Object getIdValueExp(String sqlSegment, Object parameterObject) throws OgnlException {
        List<String> conditions = Arrays.asList(StringUtils.substringsBetween(sqlSegment, StringPool.LEFT_BRACKET, StringPool.RIGHT_BRACKET));
        List<String> fields = conditions.stream().flatMap(s -> Arrays.stream(s.split(WHERE_SEPARATE))).toList();

        Object idValue = null;
        for (String field : fields) {
            String name = StringUtils.trim(StringUtils.substringBefore(field, Casts.EQ));
            if (!IdEntity.ID_FIELD_NAME.equals(name)) {
                continue;
            }
            String exp = StringUtils.trim(StringUtils.substringAfter(field, Casts.EQ));
            idValue = getOgnlValue(exp, parameterObject);
        }

        return idValue;
    }

    private Object getOgnlValue(String exp, Object parameterObject) throws OgnlException {
        Object value = Ognl.getValue(exp, parameterObject);
        if (Map.class.isAssignableFrom(value.getClass())) {
            Map<Object, Object> mapValue = Casts.cast(value);
            return mapValue.keySet().iterator().next();
        }

        return  value;
    }

    protected List<OperationDataTraceRecord> createUpdateOrDeleteRecord(String tableName,
                                                                        OperationDataType type,
                                                                        Object parameter) throws Exception {
        if (parameter instanceof MapperMethod.ParamMap<?> map) {

            Object wrapper = null;
            if (map.containsKey(Constants.WRAPPER)) {
                wrapper = map.get(Constants.WRAPPER);
            }

            Object entity = null;
            if (map.containsKey(Constants.ENTITY)) {
                entity = map.get(Constants.ENTITY);
            }

            if (Objects.isNull(entity) && Objects.isNull(wrapper)) {
                Map<String, Object> submitData = Casts.convertValue(parameter, new TypeReference<>() {});
                return List.of(createBasicOperationDataTraceRecord(type, tableName, submitData));
            }

            if (Objects.nonNull(entity) && BasicIdentification.class.isAssignableFrom(entity.getClass())) {
                BasicIdentification<Object> basicIdentification = Casts.cast(entity);
                EntityIdOperationDataTraceRecord entityRecord = createEntityIdOperationDataTraceRecord(
                        basicIdentification,
                        tableName,
                        type
                );

                return List.of(entityRecord);
            }

            if (Objects.nonNull(wrapper) && Wrapper.class.isAssignableFrom(wrapper.getClass())) {

                Wrapper<?> updateWrapper = Casts.cast(wrapper);
                String sqlSegment = updateWrapper.getSqlSegment();
                Object entityId = getIdValueExp(sqlSegment, map);

                if (Objects.nonNull(entityId)) {

                    OperationDataTraceRecord record = super.createBasicOperationDataTraceRecord(
                            type,
                            tableName,
                            new LinkedHashMap<>()
                    );

                    EntityIdOperationDataTraceRecord entityRecord = Casts.of(record, EntityIdOperationDataTraceRecord.class);

                    entityRecord.setEntityId(entityId);
                    if (OperationDataType.UPDATE.equals(type)) {
                        Map<String, Object> submitData = getUpdateModifiedMap(updateWrapper.getSqlSet(), parameter);
                        entityRecord.setSubmitData(submitData);
                    }
                    return List.of(entityRecord);
                }
            }
        } else if (BasicIdentification.class.isAssignableFrom(parameter.getClass())) {
            BasicIdentification<Object> basicIdentification = Casts.cast(parameter);
            if (Objects.nonNull(basicIdentification.getId())) {
                OperationDataTraceRecord record = createEntityIdOperationDataTraceRecord(
                        basicIdentification,
                        tableName,
                        type
                );
                return List.of(record);
            }
        }

        Map<String, Object> submitData = Casts.convertValue(parameter, new TypeReference<>() {});
        return List.of(createBasicOperationDataTraceRecord(type, tableName, submitData));

    }

    @Override
    protected List<OperationDataTraceRecord> createDeleteRecord(Delete delete, MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception {
        if (Casts.isPrimitive(parameter)) {
            OperationDataTraceRecord record = createBasicOperationDataTraceRecord(
                    OperationDataType.DELETE,
                    delete.getTable().getName(),
                    new LinkedHashMap<>()
            );
            EntityIdOperationDataTraceRecord entityRecord = Casts.of(record, EntityIdOperationDataTraceRecord.class);
            entityRecord.setEntityId(parameter);
            return List.of(entityRecord);
        }

        return createUpdateOrDeleteRecord(delete.getTable().getName(), OperationDataType.DELETE, parameter);

    }


    @Override
    public List<OperationDataTraceRecord> find(String target, Object entityId) {
        List<OperationDataTraceRecord> records = find(target);
        List<EntityIdOperationDataTraceRecord> result = records.stream().map(r -> Casts.cast(r, EntityIdOperationDataTraceRecord.class)).toList();
        return result.stream().filter(e -> e.getEntityId().equals(entityId)).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public Page<OperationDataTraceRecord> findPage(PageRequest pageRequest, String target, Object entityId) {

        List<OperationDataTraceRecord> elements = find(target, entityId);

        int fromIndex = (pageRequest.getNumber() - 1) * pageRequest.getSize();
        int toIndex = Math.min(pageRequest.getNumber() * pageRequest.getSize(), elements.size());

        return new TotalPage<>(pageRequest, elements.subList(fromIndex, toIndex), elements.size());
    }
}
