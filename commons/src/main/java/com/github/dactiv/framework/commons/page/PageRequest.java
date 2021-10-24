package com.github.dactiv.framework.commons.page;

import java.io.Serializable;

/**
 * 分页请求对象
 *
 * @author maurice.chen
 **/
public class PageRequest extends ScrollPageRequest {

    private static final long serialVersionUID = -7063877675141922463L;

    /**
     * 默认当前页数
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 页号
     */
    private int number = DEFAULT_PAGE;

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
        super(size);
        this.number = page;
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
     * 获取指定第一个返回记录行的偏移量
     *
     * @return 偏移量
     */
    public int getOffset() {
        int offset = (number - DEFAULT_PAGE) * getSize();
        if (offset < 0) {
            offset = 0;
        }
        return offset;
    }

}
