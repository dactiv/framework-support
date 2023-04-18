package com.github.dactiv.framework.mybatis.interceptor.audit;

import com.github.dactiv.framework.commons.Casts;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.util.List;

@Intercepts(
        @Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}
        )
)
public class OperationDataTraceInterceptor implements Interceptor {

    public static final String REMOVE_ESCAPE_REG = "[\t\n]";

    private static final List<SqlCommandType> SQL_COMMAND_TYPES = List.of(SqlCommandType.INSERT, SqlCommandType.UPDATE, SqlCommandType.DELETE);

    private final OperationDataTraceRepository operationDataTraceRepository;

    public OperationDataTraceInterceptor(OperationDataTraceRepository operationDataTraceRepository) {
        this.operationDataTraceRepository = operationDataTraceRepository;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();

        if (!Integer.class.isAssignableFrom(result.getClass())) {
            return result;
        }

        // 如果影响行号不大于 0 什么都不做.
        Integer affectedNumber = Casts.cast(result);
        if ( affectedNumber <= 0) {
            return result;
        }

        MappedStatement mappedStatement = Casts.cast(invocation.getArgs()[0]);
        if (!SQL_COMMAND_TYPES.contains(mappedStatement.getSqlCommandType())) {
            return result;
        }

        BoundSql boundSql = mappedStatement.getSqlSource().getBoundSql(invocation.getArgs()[1]);
        String sql = RegExUtils.replaceAll(boundSql.getSql(), REMOVE_ESCAPE_REG, StringUtils.SPACE);
        Statement statement = CCJSqlParserUtil.parse(sql);
        Object parameter = invocation.getArgs()[1];


        List<OperationDataTraceRecord> records = operationDataTraceRepository.createOperationDataTraceRecord(mappedStatement, statement, parameter);

        if (CollectionUtils.isNotEmpty(records)) {
            operationDataTraceRepository.saveOperationDataTraceRecord(records);
        }

        return result;
    }

}
