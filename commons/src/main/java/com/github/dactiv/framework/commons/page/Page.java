package com.github.dactiv.framework.commons.page;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页对象
 *
 * @author maurice.chen
 **/
public class Page<T> extends ScrollPage<T> {

    private static final long serialVersionUID = -8548642105903724207L;
    /**
     * 分页请求
     */
    private PageRequest pageRequest;

    /**
     * 分页对象
     */
    public Page() {
    }

    /**
     * 分页对象
     *
     * @param pageRequest 分页请求
     * @param elements    数据元素
     */
    public Page(PageRequest pageRequest, List<T> elements) {
        super(pageRequest, elements);
        this.pageRequest = pageRequest;
    }

    /**
     * 获取当前页号
     *
     * @return 页号
     */
    public int getNumber() {
        return pageRequest == null ? 0 : pageRequest.getNumber();
    }

    /**
     * 判断是否存在上一页
     *
     * @return true 表示存在，否则 false
     */
    public boolean hasPrevious() {
        return getNumber() > 1;
    }

    /**
     * 判断是否为首页
     *
     * @return ture 表示首页，否则 false
     */
    public boolean isFirst() {
        return !hasPrevious();
    }

}
