package com.github.dactiv.framework.commons.tree;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.BasicIdentification;

import java.util.List;
import java.util.Objects;

/**
 * 树形接口
 *
 * @param <P> 父类对象
 * @param <C> 子类对象
 *
 * @author maurice.chen
 */
public interface Tree<P, C> {

    /**
     * 根节点集合
     */
    String ROOT_VALUE = "root";

    /**
     * 父类 id 名称
     */
    String PARENT_ID_NAME = "parentId";

    /**
     * 获取孩子节点集合
     *
     * @return 孩子节点集合
     */
    List<Tree<P, C>> getChildren();

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    P getParent();

    /**
     * 是否孩子节点
     *
     * @param parent 父类实体
     *
     * @return true 为是，否则 false
     */
    default boolean isChildren(Tree<P, C> parent) {
        C c = Casts.cast(parent);

        if (!BasicIdentification.class.isAssignableFrom(c.getClass())) {
            return false;
        }

        BasicIdentification<?> identification = Casts.cast(c);
        return Objects.equals(identification.getId(), this.getParent());
    }


}
