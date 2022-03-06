package com.github.dactiv.framework.mybatis.plus.baisc.support;

import com.baomidou.mybatisplus.annotation.TableField;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.mybatis.plus.baisc.LogicDeleteEntity;

/**
 * 整形，且带版本号和逻辑删除标识的实体基类
 *
 * @author maurice.chen
 */
public class IntegerLogicDeleteEntity extends IntegerIdEntity implements LogicDeleteEntity<Integer> {

    /**
     * 是否删除标识: {@link YesOrNo#Yes} 已删除， {@link YesOrNo#No} 未删除
     */
    @TableField
    private YesOrNo deleted;

    @Override
    public YesOrNo getDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(YesOrNo deleted) {
        this.deleted = deleted;
    }
}
