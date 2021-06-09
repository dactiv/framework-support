package com.github.dactiv.framework.spring.web.result.filter;

import java.util.Map;

/**
 * 过滤属性执行器
 *
 * @author maurice.chen
 */
public interface FilterPropertyExecutor {

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
