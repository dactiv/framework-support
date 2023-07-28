package com.github.dactiv.framework.spring.web.result.error;

import com.github.dactiv.framework.commons.RestResult;

/**
 * 错误结果集解析器
 *
 * @author maurice.chen
 */
public interface ErrorResultResolver {

    /**
     * 是否支持
     *
     * @param error 错误异常
     *
     * @return true 是，否则 false
     */
    boolean isSupport(Throwable error);

    /**
     * 解析错误异常
     *
     * @param error 异常
     *
     * @return rest 结果集
     */
    RestResult<Object> resolve(Throwable error);
}
