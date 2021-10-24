package com.github.dactiv.framework.commons.page;

import java.io.Serializable;

/**
 * 分页请求对象
 *
 * @author maurice.chen
 **/
public class ScrollPageRequest implements Serializable {

    private static final long serialVersionUID = -7063877675141922463L;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_SIZE = 10;
    /**
     * 每页大小
     */
    private int size = DEFAULT_SIZE;

    /**
     * 分页请求对象
     */
    public ScrollPageRequest() {

    }

    /**
     * 分页请求对象，用于在分页查询时，通过该对象得知要查询的分页。
     *
     * @param size 内容大小
     */
    public ScrollPageRequest(int size) {
        this.size = size;
    }

    /**
     * 获取每页的内容大小
     *
     * @return 内容数量
     */
    public int getSize() {
        return size;
    }

    /**
     * 获取每页的内容大小
     *
     * @param size 内容数量
     */
    public void setSize(int size) {
        this.size = size;
    }

}
