package com.github.dactiv.framework.spring.web.interceptor;

import com.github.dactiv.framework.spring.web.SpringWebMvcAutoConfiguration;
import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * 自定义客户端http请求拦截器，用于在 {@link SpringWebMvcAutoConfiguration}
 * 自动注入类中，创建的 restTemplate 不跟 其他 ClientHttpRequestInterceptor 实现混淆。如 LoadBalancerInterceptor 等。
 *
 * @author maurice.chen
 */
public interface CustomClientHttpRequestInterceptor extends ClientHttpRequestInterceptor {
}
