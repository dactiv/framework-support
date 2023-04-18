package com.github.dactiv.framework.mybatis.interceptor.audit.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.page.TotalPage;
import com.github.dactiv.framework.mybatis.enumerate.OperationDataType;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRepository;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * 内存形式的操作数据留痕仓库实现
 *
 * @author maurice.chen
 */
public class InMemoryOperationDataTraceRepository implements OperationDataTraceRepository {

    private static final Map<String, List<OperationDataTraceRecord>> MEMORY = new LinkedHashMap<>();

    @Override
    public List<OperationDataTraceRecord> createOperationDataTraceRecord(MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception{

        if (statement instanceof Insert insert) {
            return createInsertRecord(insert, mappedStatement, statement, parameter);
        } else if (statement instanceof Update update) {
            return createUpdateRecord(update, mappedStatement, statement, parameter);
        } else if (statement instanceof Delete delete) {
            return createDeleteRecord(delete, mappedStatement, statement, parameter);
        }

        return new LinkedList<>();
    }

    protected List<OperationDataTraceRecord> createDeleteRecord(Delete delete, MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception {
        OperationDataTraceRecord result = createBasicOperationDataTraceRecord(
                OperationDataType.DELETE,
                delete.getTable().getName(),
                Casts.convertValue(parameter, new TypeReference<>() {})
        );
        return List.of(result);
    }

    protected List<OperationDataTraceRecord> createUpdateRecord(Update update, MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception {
        OperationDataTraceRecord result = createBasicOperationDataTraceRecord(
                OperationDataType.UPDATE,
                update.getTable().getName(),
                Casts.convertValue(parameter, new TypeReference<>() {})
        );
        return List.of(result);
    }

    protected List<OperationDataTraceRecord> createInsertRecord(Insert insert,
                                                                MappedStatement mappedStatement,
                                                                Statement statement,
                                                                Object parameter) throws Exception {
        OperationDataTraceRecord result = createBasicOperationDataTraceRecord(
                OperationDataType.INSERT,
                insert.getTable().getName(),
                Casts.convertValue(parameter, new TypeReference<>() {})
        );
        return List.of(result);
    }

    protected OperationDataTraceRecord createBasicOperationDataTraceRecord(OperationDataType type,
                                                                           String target,
                                                                           Map<String, Object> submitData) throws UnknownHostException {
        OperationDataTraceRecord record = new OperationDataTraceRecord();

        record.setId(UUID.randomUUID().toString());
        record.setPrincipal(InetAddress.getLocalHost().getHostAddress());
        record.setType(type);
        record.setTarget(target);
        record.setSubmitData(submitData);
        record.setRemark(record.getPrincipal() + StringUtils.SPACE + record.getCreationTime().toString() +  StringUtils.SPACE + record.getType().getName());

        return record;
    }

    @Override
    public void saveOperationDataTraceRecord(List<OperationDataTraceRecord> records) {
        for (OperationDataTraceRecord record : records) {
            List<OperationDataTraceRecord> dataTraceRecords = MEMORY.computeIfAbsent(record.getTarget(), k -> new LinkedList<>());
            dataTraceRecords.add(record);
        }
    }

    @Override
    public List<OperationDataTraceRecord> find(String target) {
        List<OperationDataTraceRecord> result =  MEMORY.get(target);
        result.sort(Comparator.comparing(StringIdEntity::getCreationTime).reversed());
        return result;
    }

    @Override
    public Page<OperationDataTraceRecord> findPage(PageRequest pageRequest, String target) {

        List<OperationDataTraceRecord> records = find(target);

        int fromIndex = (pageRequest.getNumber() - 1) * pageRequest.getSize();
        int toIndex = Math.min(pageRequest.getNumber() * pageRequest.getSize(), records.size());

        return new TotalPage<>(pageRequest, records.subList(fromIndex, toIndex), records.size());
    }
}
