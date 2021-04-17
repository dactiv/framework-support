package com.github.dactiv.framework.spring.web.interceptor;

import com.github.dactiv.framework.spring.web.SpringWebMvcSupportAutoConfiguration;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.util.List;

/**
 * 被注入的客户端请求拦截器, 为了可以让 restTemplate 的拦截器和别的框架拦截器区分开来，使用该类去进行区分
 *
 * @see SpringWebMvcSupportAutoConfiguration#restTemplate(List)
 * @see LoggingClientHttpRequestInterceptor
 *
 * @author maurice
 */
public interface InjectionClientHttpRequestInterceptor extends ClientHttpRequestInterceptor{
}
