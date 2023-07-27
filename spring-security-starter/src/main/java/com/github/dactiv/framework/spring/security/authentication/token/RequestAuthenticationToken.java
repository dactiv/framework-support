package com.github.dactiv.framework.spring.security.authentication.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * 请求认证 token
 *
 * @author maurice
 */
public class RequestAuthenticationToken extends SimpleAuthenticationToken {

    
    private static final long serialVersionUID = 8070060147431763553L;

    /**
     * http servlet request
     */
    private final HttpServletRequest httpServletRequest;

    /**
     * http servlet response
     */
    private final HttpServletResponse httpServletResponse;

    /**
     * 请求认证 token
     *
     * @param httpServletRequest  http servlet request
     * @param httpServletResponse http servlet response
     * @param token               登陆账户密码认证令牌
     * @param type                认证类型
     * @param rememberMe 是否记住我
     */
    public RequestAuthenticationToken(HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse,
                                      UsernamePasswordAuthenticationToken token,
                                      String type,
                                      boolean rememberMe) {
        super(token, type, rememberMe);

        this.httpServletResponse = httpServletResponse;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * 获取 http servlet request
     *
     * @return http servlet request
     */
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    /**
     * 获取 http servlet response
     *
     * @return http servlet response
     */
    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }
}
