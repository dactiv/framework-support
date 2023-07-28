package com.github.dactiv.framework.mybatis.interceptor.audit;

import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import net.sf.jsqlparser.statement.Statement;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.List;

/**
 * 操作数据留痕仓库
 *
 * @author maurice.chen
 */
public interface OperationDataTraceRepository {

    /**
     * 创建操作数据留痕记录
     *
     * @param mappedStatement Mapped Statement
     * @param statement sql ast
     * @param parameter 当前mybatis 参数
     *
     * @return 操作数据留痕数据
     */
    List<OperationDataTraceRecord> createOperationDataTraceRecord(MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception;

    /**
     * 保存操作数据留痕记录
     *
     * @param record 操作数据留痕记录
     */
    void saveOperationDataTraceRecord(List<OperationDataTraceRecord> record) throws Exception;

    /**
     * 查询操作记录留痕集合
     *
     * @param target 目标值
     *
     * @return 操作记录留痕集合
     */
    List<OperationDataTraceRecord> find(String target);

    /**
     * 查询操作记录留痕分页
     *
     * @param pageRequest 分页请求
     * @param target 目标值
     *
     * @return 操作记录留痕分页
     */
    Page<OperationDataTraceRecord> findPage(PageRequest pageRequest, String target);

}