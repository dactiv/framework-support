package com.github.dactiv.framework.commons.page;

import java.io.Serializable;

/**
 * 分页请求对象
 *
 * @author maurice.chen
 **/
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -7063877675141922463L;

    /**
     * 默认当前页数
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_SIZE = 10;

    /**
     * 页号
     */
    private int number = DEFAULT_PAGE;
    /**
     * 每页大小
     */
    private int size = DEFAULT_SIZE;

    /**
     * 分页请求对象
     */
    public PageRequest() {

    }

    /**
     * 分页请求对象，用于在分页查询时，通过该对象得知要查询的页数。
     *
     * @param page 页号
     * @param size 内容大小
     */
    public PageRequest(int page, int size) {
        this.number = page;
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
     * 获取当前页号
     *
     * @return 页号
     */
    public int getNumber() {
        return number;
    }

    /**
     * 设置当前页号
     *
     * @param number 页号
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * 获取每页的内容大小
     *
     * @param size 内容数量
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * 获取指定第一个返回记录行的偏移量
     *
     * @return 偏移量
     */
    public int getOffset() {
        int offset = (number - DEFAULT_PAGE) * size;
        if (offset < 0) {
            offset = 0;
        }
        return offset;
    }

}
