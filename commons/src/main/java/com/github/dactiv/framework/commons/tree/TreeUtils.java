package com.github.dactiv.framework.commons.tree;

import com.github.dactiv.framework.commons.Casts;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 树工具类，用户合并或拆解等
 *
 * @author maurice.chen
 */
public class TreeUtils {

    /**
     * 绑定泛型树
     *
     * @param list 树形集合
     * @param <P>  树形父类类型
     * @param <T>  属性孩子类型
     * @param <R>  返回类型
     *
     * @return 绑定后的树形结合
     */
    public static <P, T, R extends Tree<P, T>> List<R> buildGenericTree(List<? extends Tree<P, T>> list) {
        List<Tree<P, T>> treeList = TreeUtils.buildTree(list);

        List<R> result = new ArrayList<>();

        treeList.forEach(tree -> result.add(Casts.cast(tree)));

        return result;
    }

    /**
     * 绑定泛型树
     *
     * @param list 树形集合
     * @param <P>  树形父类类型
     * @param <T>  属性孩子类型
     * @param <R>  返回类型
     *
     * @return 绑定后的树形结合
     */
    public static <P, T, R extends Tree<P, T>> List<R> unBuildGenericTree(List<? extends Tree<P, T>> list) {
        List<Tree<P, T>> treeList = TreeUtils.unBuildTree(list);

        List<R> result = new ArrayList<>();

        treeList.forEach(tree -> result.add(Casts.cast(tree)));

        return result;
    }

    /**
     * 接触绑定树
     *
     * @param list 树形集合
     * @param <P>  树形父类类型
     * @param <T>  属性孩子类型
     *
     * @return 解除绑定后的树形结合
     */
    public static <P, T> List<Tree<P, T>> unBuildTree(List<? extends Tree<P, T>> list) {
        List<Tree<P, T>> result = new ArrayList<>();

        list.stream().flatMap(r -> unBuildTree(r.getChildren()).stream()).forEach(result::add);

        return result;
    }

    /**
     * 绑定树形
     *
     * @param list 树形集合
     * @param <P>  树形父类类型
     * @param <T>  属性孩子类型
     *
     * @return 绑定后的树形结合
     */
    public static <P, T> List<Tree<P, T>> buildTree(List<? extends Tree<P, T>> list) {
        List<Tree<P, T>> result = new ArrayList<>();

        list.stream().filter(TreeUtils::isParent).peek(root -> findChildren(root, list)).forEach(result::add);

        if (result.isEmpty()) {

            List<Tree<P, T>> children = new ArrayList<>();
            List<Tree<P, T>> clone = new ArrayList<>(list);

            list.stream().filter(root -> list.stream().anyMatch(child -> child.isChildren(root))).forEach(children::add);

            clone.removeAll(children);

            clone.stream().peek(root -> findChildren(root, list)).forEach(result::add);

        }

        return result;
    }


    /**
     * 获取孩子节点合并到父类
     *
     * @param parent 父类对象
     * @param list   树形数据集合
     * @param <P>    树形父类类型
     * @param <T>    属性孩子类型
     */
    private static <P, T> void findChildren(Tree<P, T> parent, List<? extends Tree<P, T>> list) {

        list
                .stream()
                .filter(entity -> !isParent(entity))
                .filter(entity -> entity.isChildren(parent))
                .peek(entity -> findChildren(entity, list))
                .forEach(entity -> parent.getChildren().add(entity));
    }

    /**
     * 是否父类节点
     *
     * @param tree 节点对象
     *
     * @return true 是，否则 false
     */
    public static <P, T> boolean isParent(Tree<P, T> tree) {
        return Objects.isNull(tree.getParent()) || StringUtils.isEmpty(tree.getParent().toString()) || Tree.ROOT_VALUE.equals(tree.getParent().toString());
    }

}
