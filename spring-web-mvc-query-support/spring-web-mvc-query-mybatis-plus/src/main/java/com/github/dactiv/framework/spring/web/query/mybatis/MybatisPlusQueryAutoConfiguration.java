package com.github.dactiv.framework.spring.web.query.mybatis;

import com.github.dactiv.framework.spring.web.SpringWebMvcProperties;
import com.github.dactiv.framework.spring.web.query.QueryGenerator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring mvc mybatis-plus query 自动配置实现
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@EnableConfigurationProperties(SpringWebMvcProperties.class)
@ConditionalOnProperty(prefix = "dactiv.spring.web.mvc.mybatis-plus", value = "enabled", matchIfMissing = true)
public class MybatisPlusQueryAutoConfiguration {

    @Bean
    @SuppressWarnings("rawtypes")
    @ConditionalOnMissingBean(QueryGenerator.class)
    public MybatisPlusQueryGenerator<?> mybatisPlusQueryGenerator() {
        return new MybatisPlusQueryGenerator();
    }
}
