package com.github.dactiv.framework.mybatis.plus.baisc.support;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.mybatis.plus.baisc.LogicDeleteEntity;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 整形，且带版本号和逻辑删除标识的实体基类
 *
 * @param <V> 版本号类型
 *
 * @author maurice.chen
 */
public class IntegerVersionLogicDeleteEntity<V> extends IntegerIdEntity implements VersionEntity<V, Integer>, LogicDeleteEntity<Integer> {

    @Version
    private V version;

    @TableLogic
    private YesOrNo deleted;

    @Override
    public YesOrNo getDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(YesOrNo deleted) {
        this.deleted = deleted;
    }

    @Override
    public void setVersion(V version) {
        this.version = version;
    }

    @Override
    public V getVersion() {
        return version;
    }

    @Override
    public <N extends BasicIdentification<Integer>> N ofNew(String ... ignoreProperties) {
        IntegerVersionLogicDeleteEntity<V> result = super.ofIdData(ignoreProperties);

        result.setVersion(getVersion());
        result.setDeleted(getDeleted());

        return Casts.cast(result);
    }

    @Override
    public <N extends BasicIdentification<Integer>> N ofIdData(String ... ignoreProperties) {
        List<String> ignorePropertyList = new LinkedList<>();

        CollectionUtils.addAll(ignorePropertyList, ignoreProperties);
        ignorePropertyList.add(VERSION_FIELD_NAME);
        ignorePropertyList.add(DELETE_FIELD_NAME);

        IntegerVersionLogicDeleteEntity<V> result = super.ofIdData(ignorePropertyList.toArray(new String[0]));

        result.setVersion(getVersion());
        result.setDeleted(getDeleted());

        return Casts.cast(result);
    }
}
