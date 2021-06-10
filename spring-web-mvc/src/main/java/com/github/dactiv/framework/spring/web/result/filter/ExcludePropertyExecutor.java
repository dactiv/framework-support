package com.github.dactiv.framework.spring.web.result.filter;

/**
 * 过滤属性执行器
 *
 * @author maurice.chen
 */
public interface ExcludePropertyExecutor {

    /**
     * 过滤属性
     *
     * @param id 唯一识别
     * @param data 数据对象
     *
     * @return 被过滤后的对象值
     */
    Object filter(String id, Object data);

}
