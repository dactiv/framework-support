package com.github.dactiv.framework.spring.web.query.mybatis.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.web.query.mybatis.MybatisPlusQueryGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * 简单封装的基础实体业务逻辑基类，该类用于不实现 service 接口的情况直接继承使用，封装一些常用的方法
 *
 * @param <M> 映射 BaseMapper 的 dao 实现
 * @param <T> 映射 BaseMapper dao 实体实现
 */
public class BasicService<M extends BaseMapper<T>, T extends BasicIdentification<? extends Serializable>> {

    @Autowired
    protected M baseMapper;

    protected Class<T> entityClass;

    protected Class<M> mapperClass;

    public BasicService() {
        mapperClass = ReflectionUtils.getGenericClass(this, BigDecimal.ZERO.intValue());
        entityClass = ReflectionUtils.getGenericClass(this, BigDecimal.ONE.intValue());
    }

    public int save(Iterable<T> entities) {
        return save(entities, true);
    }

    public int save(Iterable<T> entities, boolean errorThrow) {
        int result = 0;
        for (T e : entities) {
            if (!save(e) && errorThrow) {
                String msg = "保存 id 为 [" + e.getId() + "] 的 [" + getEntityClass() + "] 数据不成功，内容为 [" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    public boolean save(T entity) {
        if (Objects.isNull(entity.getId())) {
            return insert(entity);
        }
        return updateById(entity);
    }

    public int insert(Iterable<T> entities) {
        return insert(entities, true);
    }

    public int insert(Iterable<T> entities, boolean errorThrow) {
        int result = 0;
        for (T e : entities) {
            if (!insert(e) && errorThrow) {
                String msg = "新增 id 为 [" + e.getId() + "] 的 [" + getEntityClass() + "] 数据不成功，内容为 [" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    public boolean insert(T entity) {
        return baseMapper.insert(entity) > 0;
    }

    public int updateById(Iterable<T> entities) {
        return updateById(entities, true);
    }

    public int updateById(Iterable<T> entities, boolean errorThrow) {
        int result = 0;
        for (T e : entities) {
            if (!insert(e) && errorThrow) {
                String msg = "更新 id 为 [" + e.getId() + "] 的 [" + getEntityClass() + "] 数据不成功，内容为 [" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    public boolean updateById(T entity) {
        return baseMapper.updateById(entity) > 0;
    }

    public int update(Iterable<T> entity, Wrapper<T> wrapper) {
        return update(entity, wrapper, true);
    }

    public int update(Iterable<T> entity, Wrapper<T> wrapper, boolean errorThrow) {
        int result = 0;

        for (T e : entity) {
            if (!update(e, wrapper) && errorThrow) {
                String msg = "更新 id 为 [" + e.getId() + "] 的 [" + getEntityClass() + "] 数据不成功，内容为 [" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    public boolean update(T entity, Wrapper<T> wrapper) {
        return baseMapper.update(entity, wrapper) > 0;
    }

    public long count() {
        return count(null);
    }

    public long count(Wrapper<T> wrapper) {
        return baseMapper.selectCount(wrapper);
    }

    public List<T> find() {
        return find(null);
    }

    public List<T> find(Wrapper<T> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    public T findOne(Wrapper<T> wrapper) {
        return baseMapper.selectOne(wrapper);
    }

    public Page<T> findPage(PageRequest pageRequest) {
        return findPage(pageRequest, null);
    }

    public Page<T> findPage(PageRequest pageRequest, Wrapper<T> wrapper) {
        IPage<T> result = baseMapper.selectPage(
                MybatisPlusQueryGenerator.createQueryPage(pageRequest),
                wrapper
        );

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    public int deleteById(Iterable<? extends Serializable> ids) {
        return deleteById(ids, true);
    }

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

    public boolean deleteById(Serializable id) {
        return baseMapper.deleteById(id) > 0;
    }

    public int deleteByEntity(Iterable<T> entities) {
        return deleteByEntity(entities, true);
    }

    public int deleteByEntity(Iterable<T> entities, boolean errorThrow) {
        int result = 0;
        for (T e : entities) {
            if (!deleteByEntity(e) && errorThrow) {
                String msg = "删除 id 为 [" + e.getId() + "] 的 [" + getEntityClass() + "] 数据不成功, 内容为[" + e + "]";
                throw new SystemException(msg);
            } else {
                result++;
            }
        }
        return result;
    }

    public boolean deleteByEntity(T entity) {
        return baseMapper.deleteById(entity) > 0;
    }

    /**
     * 获取实体
     *
     * @param id 主键 id
     *
     * @return 实体
     */
    public T get(Serializable id) {
        return baseMapper.selectById(id);
    }

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
