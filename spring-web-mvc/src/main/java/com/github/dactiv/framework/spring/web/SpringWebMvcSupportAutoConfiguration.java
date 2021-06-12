package com.github.dactiv.framework.spring.web;


import com.fasterxml.jackson.databind.SerializationConfig;
import com.github.dactiv.framework.spring.web.argument.DeviceHandlerMethodArgumentResolver;
import com.github.dactiv.framework.spring.web.argument.GenericsListHandlerMethodArgumentResolver;
import com.github.dactiv.framework.spring.web.endpoint.EnumerateEndpoint;
import com.github.dactiv.framework.spring.web.interceptor.CustomClientHttpRequestInterceptor;
import com.github.dactiv.framework.spring.web.interceptor.LoggingClientHttpRequestInterceptor;
import com.github.dactiv.framework.spring.web.mobile.DeviceResolverHandlerInterceptor;
import com.github.dactiv.framework.spring.web.result.RestResponseBodyAdvice;
import com.github.dactiv.framework.spring.web.result.RestResultErrorAttributes;
import com.github.dactiv.framework.spring.web.result.filter.FilterResultAnnotationBuilder;
import com.github.dactiv.framework.spring.web.result.filter.FilterResultHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * spring mvc 自动配置实现
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@EnableConfigurationProperties(SpringWebSupportProperties.class)
@ConditionalOnProperty(prefix = "spring.web.mvc.support", value = "enabled", matchIfMissing = true)
public class SpringWebMvcSupportAutoConfiguration {

    @Autowired(required = false)
    private List<InfoContributor> infoContributors;

    @Configuration
    @EnableConfigurationProperties(SpringWebSupportProperties.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class DefaultWebMvcConfigurer implements WebMvcConfigurer {

        @Autowired
        private SpringWebSupportProperties properties;

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new GenericsListHandlerMethodArgumentResolver(getValidator()));
            argumentResolvers.add(new DeviceHandlerMethodArgumentResolver());
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new DeviceResolverHandlerInterceptor());
            registry.addInterceptor(new FilterResultHandlerInterceptor(properties));
        }
    }

    @Bean
    @ConditionalOnMissingBean(RestResultErrorAttributes.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public RestResultErrorAttributes servletRestResultErrorAttributes() {
        return new RestResultErrorAttributes();
    }


    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public RestTemplate restTemplate(List<CustomClientHttpRequestInterceptor> clientHttpRequestInterceptors) {
        RestTemplate restTemplate = new RestTemplate();

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(clientHttpRequestInterceptors);

        interceptors.add(new LoggingClientHttpRequestInterceptor());
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(RestResponseBodyAdvice.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public RestResponseBodyAdvice restResponseBodyAdvice(SpringWebSupportProperties properties) {
        return new RestResponseBodyAdvice(properties);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer filterResultJackson2ObjectMapperBuilderCustomizer(SpringWebSupportProperties properties) {
        return jacksonObjectMapperBuilder -> {

            FilterResultAnnotationBuilder filterResultAnnotationBuilder = new FilterResultAnnotationBuilder(properties.getBasePackages());

            jacksonObjectMapperBuilder.annotationIntrospector(filterResultAnnotationBuilder);

            SerializationConfig config = jacksonObjectMapperBuilder.build().getSerializationConfig();

            jacksonObjectMapperBuilder.filters(filterResultAnnotationBuilder.getFilterProvider(config));
        };
    }


    @Bean
    @ConfigurationProperties("enumerate")
    public EnumerateEndpoint enumerateEndpoint() {
        return new EnumerateEndpoint(infoContributors);
    }

}
