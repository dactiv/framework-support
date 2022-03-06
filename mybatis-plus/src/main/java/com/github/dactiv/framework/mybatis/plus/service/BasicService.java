package com.github.dactiv.framework.mybatis.plus.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 简单封装的基础实体业务逻辑基类，该类用于不实现 service 接口的情况直接继承使用，封装一些常用的方法
 *
 * @param <M> 映射 BaseMapper 的 dao 实现
 * @param <T> 映射 BaseMapper dao 实体实现
 */
public class BasicService<M extends BaseMapper<T>, T extends Serializable> {

    /**
     * mapper 实例
     */
    protected M baseMapper;

    /**
     * 实体类型
     */
    protected final Class<T> entityClass;

    /**
     * mapper 类型
     */
    protected  final Class<M> mapperClass;

    public BasicService() {
        mapperClass = ReflectionUtils.getGenericClass(this, BigDecimal.ZERO.intValue());
        entityClass = ReflectionUtils.getGenericClass(this, BigDecimal.ONE.intValue());
    }

    /**
     * 保存数据，如果实体实现 {@link BasicIdentification} 接口，并通过 {@link BasicIdentification#getId()}
     * 得到的值为 null 时会新增数据，会通过 {@link BasicIdentification#getId()} 去更新当前数据。
     *
     * @param entities 可迭代的实体信息
     *
     * @return 影响行数
     */
    public int save(Collection<T> entities) {
        return save(entities, false);
    }

    /**
     * 保存数据，如果实体实现 {@link BasicIdentification} 接口，并通过 {@link BasicIdentification#getId()}
     * 得到的值为 null 时会新增数据，会通过 {@link BasicIdentification#getId()} 去更新当前数据。
     *
     * @param entities 可迭代的实体信息
     * @param errorThrow true 如果执行过程中存在的影响行数小于 1 时抛出异常， 否则 false
     *
     * @return 影响行数
     */
    public int save(Collection<T> entities, boolean errorThrow) {
        return executeIterable(entities, errorThrow, (e) -> save(e) > 0, "save");
    }

    /**
     * 保存数据，如果实体实现 {@link BasicIdentification} 接口，并通过 {@link BasicIdentification#getId()}
     * 得到的值为 null 时会新增数据，会通过 {@link BasicIdentification#getId()} 去更新当前数据。
     *
     * @param entity 实体内容
     *
     * @return 影响行数
     */
    public int save(T entity) {

        if (!BasicIdentification.class.isAssignableFrom(entity.getClass())) {
            return insert(entity);
        }

        BasicIdentification<?> basicIdentification = Casts.cast(entity);
        if (Objects.isNull(basicIdentification.getId())) {
            return insert(entity);
        }

        return updateById(entity);

    }

    /**
     * 新增数据，如果执行过程中存在的影响行数小于 1 时，抛出异常
     *
     * @param entities 可迭代的实体信息
     *
     * @return 影响行数
     */
    public int insert(Collection<T> entities) {
        return insert(entities, false);
    }

    /**
     * 新增数据
     *
     * @param entities 可迭代的实体信息
     * @param errorThrow true 如果执行过程中存在的影响行数小于 1 时抛出异常，否则 false
     *
     * @return 影响行数
     */
    public int insert(Collection<T> entities, boolean errorThrow) {
        return executeIterable(entities, errorThrow, (e) -> insert(e) > 0, "insert");
    }

    /**
     * 新增数据
     *
     * @param entity 实体信息
     *
     * @return 影响行数
     *
     */
    public int insert(T entity) {
        return baseMapper.insert(entity);
    }

    /**
     * 通过主键 id 更新数据，如果执行过程中存在的影响行数小于 1 时，抛出异常
     *
     * @param entities 可迭代的实体信息
     *
     * @return 影响行数
     */
    public int updateById(Collection<T> entities) {
        return updateById(entities, false);
    }

    /**
     * 通过主键 id 更新数据
     *
     * @param entities 可迭代的实体信息
     * @param errorThrow true 如果执行过程中存在的影响行数小于 1 时抛出异常，否则 false
     *
     * @return 影响行数
     */
    public int updateById(Collection<T> entities, boolean errorThrow) {
        return executeIterable(entities, errorThrow, (e) -> updateById(e) > 0, "updateById");
    }

    /**
     * 执行可迭代的数据内容
     *
     * @param iterable 可迭代的数据
     * @param errorThrow true 如果执行过程中存在的影响行数小于 1 时抛出异常，否则 false
     * @param predicate 执行内容断言
     * @param name 执行名称
     *
     * @return 影响行数
     */
    public int executeIterable(Collection<T> iterable, boolean errorThrow, Predicate<T> predicate, String name) {
        int result = 0;
        for (T e : iterable) {
            if (!predicate.test(e) && errorThrow) {
                String msg = "执行 [" + getEntityClass() + "] 的 [" + name + " ] 操作为对数据发生任何变化, 数据内容为 [" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    /**
     * 通过主键 id 更新数据
     *
     * @param entity 实体信息
     *
     * @return 影响行数
     */
    public int updateById(T entity) {

        if (VersionEntity.class.isAssignableFrom(entity.getClass())) {
            VersionEntity<?, T> versionEntity = Casts.cast(entity);
            if (Objects.nonNull(versionEntity.getVersion())) {
                return updateById(List.of(entity), true);
            }
        }

        return baseMapper.updateById(entity);
    }

    /**
     * 根据 where 条件 参数更新数据，如果执行过程中存在的影响行数小于 1 时抛出异常
     *
     * @param entities 可迭代的实体信息
     * @param wrapper where 条件
     *
     * @return 影响行数
     */
    public int update(Collection<T> entities, Wrapper<T> wrapper) {
        return update(entities, wrapper, false);
    }

    /**
     * 根据 where 条件 参数更新数据
     *
     * @param entities 可迭代的实体信息
     * @param wrapper where 条件
     * @param errorThrow 如果执行过程中存在的影响行数小于 1 时，抛出异常
     *
     * @return 影响行数
     */
    public int update(Collection<T> entities, Wrapper<T> wrapper, boolean errorThrow) {
        return executeIterable(entities, errorThrow, (e) -> update(e, wrapper) > 0, "update");
    }

    /**
     * 根据 where 条件 参数更新数据
     *
     * @param entity 实体内容
     * @param wrapper where 条件
     *
     * @return 影响行数
     */
    public int update(T entity, Wrapper<T> wrapper) {
        return baseMapper.update(entity, wrapper);
    }

    /**
     * 统计数据量
     *
     * @return 数据量
     */
    public long count() {
        return count(null);
    }

    /**
     * 统计数据量
     *
     * @param wrapper where 条件
     *
     * @return 数据量
     */
    public long count(Wrapper<T> wrapper) {
        return baseMapper.selectCount(wrapper);
    }

    /**
     * 查找全部数据
     *
     * @return 数据集合
     */
    public List<T> find() {
        return find(null);
    }

    /**
     * 查找数据
     *
     * @param wrapper where 条件
     *
     * @return 数据集合
     */
    public List<T> find(Wrapper<T> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    /**
     * 查找数据
     *
     * @param wrapper where 条件
     *
     * @return 数据集合
     */
    public <R> List<R> findObjects(Wrapper<T> wrapper, Class<R> returnType) {
        List<Object> result = baseMapper.selectObjs(wrapper);

        if (CollectionUtils.isNotEmpty(result)) {
            return result
                    .stream()
                    .map(o -> Casts.cast(o, returnType))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    /**
     * 查找单个数据
     *
     * @param wrapper where 条件
     *
     * @return 数据内容
     */
    public T findOne(Wrapper<T> wrapper) {
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 查找单个数据
     *
     * @param wrapper where 条件
     * @param returnType 数据返回类型 class
     * @param <R> 数据返回类型
     *
     * @return 数据内容
     */
    public <R> R findOneObject(Wrapper<T> wrapper, Class<R> returnType) {
        List<Object> result = baseMapper.selectObjs(wrapper);

        if (CollectionUtils.isNotEmpty(result)) {
            if (result.size() != 1) {
                throw ExceptionUtils.mpe("One record is expected, but the query result is multiple records");
            }
            return Casts.cast(result.iterator().next(), returnType);
        }

        return null;
    }

    /**
     * 查找分页数据
     *
     * @param pageRequest 分页请求
     *
     * @return 分页内容
     */
    public Page<T> findPage(PageRequest pageRequest) {
        return findPage(pageRequest, null);
    }

    /**
     * 查找分页数据
     *
     * @param pageRequest 分页请求
     * @param wrapper where 条件
     *
     * @return 分页内容
     */
    public Page<T> findPage(PageRequest pageRequest, Wrapper<T> wrapper) {
        IPage<T> result = baseMapper.selectPage(
                MybatisPlusQueryGenerator.createQueryPage(pageRequest),
                wrapper
        );

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    /**
     * 根据主键 id 删除数据，如果执行过程中存在的影响行数小于 1 时，抛出异常
     *
     * @param ids 主键 id 集合
     *
     * @return 影响行数
     */
    public int deleteById(Collection<? extends Serializable> ids) {
        return deleteById(ids, false);
    }

    /**
     * 根据主键 id 删除数据，
     *
     * @param ids 主键 id 集合
     * @param errorThrow true 如果执行过程中存在的影响行数小于 1 时抛出异常，否则 false
     *
     * @return 影响行数
     */
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        Collection<Serializable> collection = new LinkedList<>();
        CollectionUtils.addAll(collection, ids);
        int result = baseMapper.deleteBatchIds(collection);
        if (result != collection.size() && errorThrow) {
            String msg = "删除 id 为 [" + collection + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return result;
    }

    /**
     * 根据主键 id 删除数据，
     *
     * @param id 主键 id
     *
     * @return 影响行数
     */
    public int deleteById(Serializable id) {
        return baseMapper.deleteById(id);
    }

    /**
     * 根据实体主键 id 删除数据，如果执行过程中存在的影响行数小于 1 时抛出异常
     *
     * @param entities 可迭代的实体信息
     *
     * @return 影响行数
     */
    public int deleteByEntity(Collection<T> entities) {
        return deleteByEntity(entities, true);
    }

    /**
     * 根据实体主键 id 删除数据
     *
     * @param entities 可迭代的实体信息
     * @param errorThrow true 如果执行过程中存在的影响行数小于 1 时抛出异常，否则 false
     *
     * @return 影响行数
     */
    public int deleteByEntity(Collection<T> entities, boolean errorThrow) {
        return executeIterable(entities, errorThrow, (e) -> deleteByEntity(e) > 0, "deleteByEntity");
    }

    /**
     * 根据实体主键 id 删除数据
     *
     * @param entity 实体
     *
     * @return 影响行数
     */
    public int deleteByEntity(T entity) {
        return baseMapper.deleteById(entity);
    }

    /**
     * 根据 where 条件删除数据
     *
     * @param wrapper where 条件
     *
     * @return 影响行数
     */
    public int delete(Wrapper<T> wrapper) {
        return baseMapper.delete(wrapper);
    }

    /**
     * 根据主键 id 获取实体
     *
     * @param id 主键 id
     *
     * @return 实体
     */
    public T get(Serializable id) {
        return baseMapper.selectById(id);
    }

    /**
     * 根据主键 id 获取实体
     *
     * @param ids 主键 id 集合
     *
     * @return 实体集合
     */
    public List<T> get(Collection<? extends Serializable> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 链式更改
     * @return UpdateWrapper 的包装类
     */
    public UpdateChainWrapper<T> update() {
        return ChainWrappers.updateChain(getBaseMapper());
    }

    /**
     * 链式更改 lambda 式
     *
     * @return LambdaUpdateWrapper 的包装类
     */
    public LambdaUpdateChainWrapper<T> lambdaUpdate() {
        return ChainWrappers.lambdaUpdateChain(getBaseMapper());
    }

    /**
     * 链式更改
     * @return UpdateWrapper 的包装类
     */
    public QueryChainWrapper<T> query() {
        return ChainWrappers.queryChain(getBaseMapper());
    }

    /**
     * 链式更改 lambda 式
     *
     * @return LambdaUpdateWrapper 的包装类
     */
    public LambdaQueryChainWrapper<T> lambdaQuery() {
        return ChainWrappers.lambdaQueryChain(getBaseMapper());
    }

    /**
     * 设置 mapper 实现
     * @param baseMapper mapper 实现
     */
    @Autowired
    public void setBaseMapper(M baseMapper) {
        this.baseMapper = baseMapper;
    }

    /**
     * 获取 mapper 实现
     *
     * @return mapper 实现
     */
    public M getBaseMapper() {
        return baseMapper;
    }

    /**
     * 获取实体类型
     *
     * @return 实体类型
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * 获取 mapper 类型
     *
     * @return mapper 类型
     */
    public Class<M> getMapperClass() {
        return mapperClass;
    }
}
