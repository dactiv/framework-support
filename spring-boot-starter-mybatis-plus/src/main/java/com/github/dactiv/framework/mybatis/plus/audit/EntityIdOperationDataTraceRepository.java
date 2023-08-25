package com.github.dactiv.framework.mybatis.plus.audit;

import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRepository;

import java.util.List;

/**
 * 带实体 ID 的操作数据留痕仓库实现
 *
 * @author maurice.chen
 */
public interface EntityIdOperationDataTraceRepository extends OperationDataTraceRepository {

    /**
     * 根据目标值和实体 id 查询操作数据留痕集合
     *
     * @param target 目标值
     * @param entityId 实体 id
     *
     * @return 操作数据留痕集合
     */
    List<OperationDataTraceRecord> find(String target, Object entityId);

    /**
     * 根据目标值和实体 id 查询操作数据留痕分页
     *
     * @param pageRequest  分页请求
     * @param target 目标值
     * @param entityId 实体 id
     *
     * @return 操作数据留痕分页
     *
     */
    Page<OperationDataTraceRecord> findPage(PageRequest pageRequest, String target, Object entityId);
}
