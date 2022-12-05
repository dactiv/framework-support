package com.github.dactiv.framework.spring.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.framework.commons.jackson.serializer.DoubleSerializer;
import com.github.dactiv.framework.commons.jackson.serializer.NameEnumSerializer;
import com.github.dactiv.framework.commons.jackson.serializer.NameValueEnumSerializer;
import com.github.dactiv.framework.commons.jackson.serializer.ValueEnumSerializer;
import com.github.dactiv.framework.spring.web.argument.DeviceHandlerMethodArgumentResolver;
import com.github.dactiv.framework.spring.web.argument.GenericsListHandlerMethodArgumentResolver;
import com.github.dactiv.framework.spring.web.device.DeviceResolverRequestFilter;
import com.github.dactiv.framework.spring.web.endpoint.EnumerateEndpoint;
import com.github.dactiv.framework.spring.web.interceptor.CustomClientHttpRequestInterceptor;
import com.github.dactiv.framework.spring.web.interceptor.LoggingClientHttpRequestInterceptor;
import com.github.dactiv.framework.spring.web.jackson.LocalDateTimeTimestampSerializer;
import com.github.dactiv.framework.spring.web.jackson.LocalDateTimestampSerializer;
import com.github.dactiv.framework.spring.web.jackson.LocalTimeSecondOfDaySerializer;
import com.github.dactiv.framework.spring.web.result.RestResponseBodyAdvice;
import com.github.dactiv.framework.spring.web.result.RestResultErrorAttributes;
import com.github.dactiv.framework.spring.web.result.error.ErrorResultResolver;
import com.github.dactiv.framework.spring.web.result.error.support.BindingResultErrorResultResolver;
import com.github.dactiv.framework.spring.web.result.error.support.ErrorCodeResultResolver;
import com.github.dactiv.framework.spring.web.result.error.support.MissingServletRequestParameterResolver;
import com.github.dactiv.framework.spring.web.result.filter.FilterResultAnnotationBuilder;
import com.github.dactiv.framework.spring.web.result.filter.FilterResultSerializerProvider;
import com.github.dactiv.framework.spring.web.result.filter.holder.ClearFilterResultHolderFilter;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.UndertowWebSocketServletWebServerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * spring mvc 自动配置实现
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@EnableConfigurationProperties({SpringWebMvcProperties.class, JacksonProperties.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "dactiv.spring.web.mvc", value = "enabled", matchIfMissing = true)
public class SpringWebMvcAutoConfiguration {

    @Configuration
    @EnableConfigurationProperties(SpringWebMvcProperties.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class DefaultWebMvcConfigurer extends UndertowWebSocketServletWebServerCustomizer implements WebMvcConfigurer {

        private final SpringWebMvcProperties properties;

        public DefaultWebMvcConfigurer(SpringWebMvcProperties properties) {
            this.properties = properties;
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new GenericsListHandlerMethodArgumentResolver(getValidator()));
            argumentResolvers.add(new DeviceHandlerMethodArgumentResolver());
        }

        @Override
        public void customize(UndertowServletWebServerFactory factory) {
            super.customize(factory);
            factory.addDeploymentInfoCustomizers(deploymentInfo -> {
                WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
                webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(false, properties.getWebSocketDeploymentBuffers()));
                deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);
            });
        }
    }

    @Bean
    @ConditionalOnMissingBean(RestResultErrorAttributes.class)
    public RestResultErrorAttributes servletRestResultErrorAttributes(List<ErrorResultResolver> resultResolvers,
                                                                      SpringWebMvcProperties properties) {
        return new RestResultErrorAttributes(
                resultResolvers,
                properties.getSupportException(),
                properties.getSupportHttpStatus()
        );
    }

    @Bean
    @ConditionalOnMissingBean(RestResponseBodyAdvice.class)
    public RestResponseBodyAdvice restResponseBodyAdvice(SpringWebMvcProperties properties) {
        return new RestResponseBodyAdvice(properties);
    }

    @Bean
    public DeviceResolverRequestFilter deviceResolverRequestFilter() {
        return new DeviceResolverRequestFilter();
    }

    @Bean
    public BindingResultErrorResultResolver bindingResultErrorResultResolver() {
        return new BindingResultErrorResultResolver();
    }

    @Bean
    public MissingServletRequestParameterResolver missingServletRequestParameterResolver() {
        return new MissingServletRequestParameterResolver(MissingServletRequestParameterResolver.DEFAULT_OBJECT_NAME);
    }

    @Bean
    public ErrorCodeResultResolver errorCodeResultResolver() {
        return new ErrorCodeResultResolver();
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
    public ObjectMapper filterResultObjectMapper(Jackson2ObjectMapperBuilder builder,
                                                 JacksonProperties jacksonProperties,
                                                 SpringWebMvcProperties properties) {

        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.setSerializerProvider(new FilterResultSerializerProvider());

        FilterResultAnnotationBuilder annotationBuilder = new FilterResultAnnotationBuilder(properties.getFilterViewBasePackages());

        objectMapper.setFilterProvider(annotationBuilder.getFilterProvider(objectMapper.getSerializationConfig()));
        objectMapper.setAnnotationIntrospector(annotationBuilder);

        SimpleModule module = new SimpleModule();

        module.addSerializer(NameValueEnum.class, new NameValueEnumSerializer());
        module.addSerializer(ValueEnum.class, new ValueEnumSerializer());
        module.addSerializer(NameEnum.class, new NameEnumSerializer());

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(properties.getJsonNumberMaximumFractionDigits());
        numberFormat.setRoundingMode(properties.getJsonNumberRoundingMode());

        DoubleSerializer doubleSerializer = new DoubleSerializer(numberFormat);
        module.addSerializer(Double.class, doubleSerializer);

        Map<SerializationFeature, Boolean> map = jacksonProperties.getSerialization();
        Boolean isWriteDatesAsTimestamps = map.get(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        if (Objects.nonNull(isWriteDatesAsTimestamps) && isWriteDatesAsTimestamps) {
            module.addSerializer(LocalDate.class, new LocalDateTimestampSerializer(jacksonProperties));
            module.addSerializer(LocalDateTime.class, new LocalDateTimeTimestampSerializer(jacksonProperties));
            module.addSerializer(LocalTime.class, new LocalTimeSecondOfDaySerializer());
        }

        objectMapper.registerModule(module);

        if (properties.isUseFilterResultObjectMapperToCastsClass()) {
            Casts.setObjectMapper(objectMapper);
        }

        return objectMapper;
    }

    @Bean
    @ConfigurationProperties("dactiv.enumerate")
    public EnumerateEndpoint enumerateEndpoint(ObjectProvider<InfoContributor> infoContributor) {
        return new EnumerateEndpoint(infoContributor.stream().collect(Collectors.toList()));
    }

    @Bean
    public ClearFilterResultHolderFilter clearFilterResultHolderFilter() {
        return new ClearFilterResultHolderFilter();
    }

}
