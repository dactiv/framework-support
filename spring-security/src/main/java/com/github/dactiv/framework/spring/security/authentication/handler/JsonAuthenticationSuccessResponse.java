package com.github.dactiv.framework.spring.security.authentication.handler;

import com.github.dactiv.framework.commons.RestResult;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 响应 json 数据的认证成功处理实现后的数据追加接口
 *
 * @author maurice.chen
 */
public interface JsonAuthenticationSuccessResponse {

    /**
     * 设置响应信息
     *
     * @param result  响应内容
     * @param request http 请求
     */
    void setting(RestResult<Object> result, HttpServletRequest request);
}
