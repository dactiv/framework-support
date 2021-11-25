package com.github.dactiv.framework.mybatis.plus.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 简单封装的基础实体业务逻辑基类，该类用于不实现 service 接口的情况直接继承使用，封装一些常用的方法
 *
 * @param <M> 映射 BaseMapper 的 dao 实现
 * @param <T> 映射 BaseMapper dao 实体实现
 */
public class BasicService<M extends BaseMapper<T>, T extends Serializable> {

    @Autowired
    protected M baseMapper;

    /**
     * 实体类型
     */
    protected Class<T> entityClass;

    /**
     * mapper 类型
     */
    protected Class<M> mapperClass;

    public BasicService() {
        mapperClass = ReflectionUtils.getGenericClass(this, BigDecimal.ZERO.intValue());
        entityClass = ReflectionUtils.getGenericClass(this, BigDecimal.ONE.intValue());
    }

    /**
     * 保存数据，如果实体实现 {@link BasicIdentification} 接口，并通过 {@link BasicIdentification#getId()}
     * 得到的值为 null 时会新增数据，会通过 {@link BasicIdentification#getId()} 去更新当前数据。
     *
     * <p>
     * 注意：
     * 如果执行过程中存在的影响行数小于 1 时抛出异常。
     * </p>
     *
     * @param entities 可迭代的实体信息
     *
     * @return 影响行数
     */
    public int save(Iterable<T> entities) {
        return save(entities, true);
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
    public int save(Iterable<T> entities, boolean errorThrow) {
        int result = 0;
        for (T e : entities) {
            if (!save(e) && errorThrow) {
                String msg = "保存 [" + getEntityClass() + "] 数据不成功，内容为 [" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    /**
     * 保存数据，如果实体实现 {@link BasicIdentification} 接口，并通过 {@link BasicIdentification#getId()}
     * 得到的值为 null 时会新增数据，会通过 {@link BasicIdentification#getId()} 去更新当前数据。
     *
     * @param entity 实体内容
     *
     * @return 如果执行过程中存在的影响行数小于 1 时返回 false，否则返回 true
     */
    public boolean save(T entity) {

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
    public int insert(Iterable<T> entities) {
        return insert(entities, true);
    }

    /**
     * 新增数据
     *
     * @param entities 可迭代的实体信息
     * @param errorThrow true 如果执行过程中存在的影响行数小于 1 时抛出异常，否则 false
     *
     * @return 影响行数
     */
    public int insert(Iterable<T> entities, boolean errorThrow) {
        int result = 0;
        for (T e : entities) {
            if (!insert(e) && errorThrow) {
                String msg = "新增 [" + getEntityClass() + "] 数据不成功，内容为 [" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    /**
     * 新增数据
     *
     * @param entity 实体信息
     *
     * @return 如果执行过程中存在的影响行数小于 1 时返回 false，否则返回 true
     *
     */
    public boolean insert(T entity) {
        return baseMapper.insert(entity) > 0;
    }

    /**
     * 通过主键 id 更新数据，如果执行过程中存在的影响行数小于 1 时，抛出异常
     *
     * @param entities 可迭代的实体信息
     *
     * @return 影响行数
     */
    public int updateById(Iterable<T> entities) {
        return updateById(entities, true);
    }

    /**
     * 通过主键 id 更新数据
     *
     * @param entities 可迭代的实体信息
     * @param errorThrow true 如果执行过程中存在的影响行数小于 1 时抛出异常，否则 false
     *
     * @return 影响行数
     */
    public int updateById(Iterable<T> entities, boolean errorThrow) {
        int result = 0;
        for (T e : entities) {
            if (!updateById(e) && errorThrow) {
                String msg = "更新 [" + getEntityClass() + "] 数据不成功，内容为 [" + e + "]";
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
     * @return 如果执行过程中存在的影响行数小于 1 时返回 false，否则返回 true
     */
    public boolean updateById(T entity) {
        return baseMapper.updateById(entity) > 0;
    }

    /**
     * 根据 根据 whereEntity 条件 参数更新数据，如果执行过程中存在的影响行数小于 1 时抛出异常
     *
     * @param entities 可迭代的实体信息
     * @param wrapper whereEntity 条件
     *
     * @return 影响行数
     */
    public int update(Iterable<T> entities, Wrapper<T> wrapper) {
        return update(entities, wrapper, true);
    }

    /**
     * 根据 根据 whereEntity 条件 参数更新数据
     *
     * @param entities 可迭代的实体信息
     * @param wrapper whereEntity 条件
     * @param errorThrow 如果执行过程中存在的影响行数小于 1 时，抛出异常
     *
     * @return 影响行数
     */
    public int update(Iterable<T> entities, Wrapper<T> wrapper, boolean errorThrow) {
        int result = 0;
        for (T e : entities) {
            if (!update(e, wrapper) && errorThrow) {
                String msg = "更新 [" + getEntityClass() + "] 数据不成功，内容为 [" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    /**
     * 根据 根据 whereEntity 条件 参数更新数据
     *
     * @param entity 实体内容
     * @param wrapper whereEntity 条件
     *
     * @return 如果执行过程中存在的影响行数小于 1 时返回 false，否则返回 true
     */
    public boolean update(T entity, Wrapper<T> wrapper) {
        return baseMapper.update(entity, wrapper) > 0;
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
    public int deleteById(Iterable<? extends Serializable> ids) {
        return deleteById(ids, true);
    }

    /**
     * 根据主键 id 删除数据，
     *
     * @param ids 主键 id 集合
     * @param errorThrow true 如果执行过程中存在的影响行数小于 1 时抛出异常，否则 false
     *
     * @return 影响行数
     */
    public int deleteById(Iterable<? extends Serializable> ids, boolean errorThrow) {
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
     * @return 如果执行过程中存在的影响行数小于 1 时返回 false，否则返回 true
     */
    public boolean deleteById(Serializable id) {
        return baseMapper.deleteById(id) > 0;
    }

    /**
     * 根据实体主键 id 删除数据，如果执行过程中存在的影响行数小于 1 时抛出异常
     *
     * @param entities 可迭代的实体信息
     *
     * @return 影响行数
     */
    public int deleteByEntity(Iterable<T> entities) {
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
    public int deleteByEntity(Iterable<T> entities, boolean errorThrow) {
        int result = 0;
        for (T e : entities) {
            if (!deleteByEntity(e) && errorThrow) {
                String msg = "删除 [" + getEntityClass() + "] 数据不成功, 内容为[" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    /**
     * 根据实体主键 id 删除数据
     *
     * @param entity 实体
     *
     * @return 如果执行过程中存在的影响行数小于 1 时返回 false，否则返回 true
     */
    public boolean deleteByEntity(T entity) {
        return baseMapper.deleteById(entity) > 0;
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
    public List<T> get(List<? extends Serializable> ids) {
        return baseMapper.selectBatchIds(ids);
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
