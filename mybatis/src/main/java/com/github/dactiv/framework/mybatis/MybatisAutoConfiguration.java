package com.github.dactiv.framework.mybatis;

import com.github.dactiv.framework.mybatis.handler.JacksonListReferenceTypeHandler;
import com.github.dactiv.framework.mybatis.handler.JacksonTypeHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring mvc mybatis-plus query 自动配置实现
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnProperty(prefix = "dactiv.mybatis", value = "enabled", matchIfMissing = true)
public class MybatisAutoConfiguration {

    @Bean
    public JacksonListReferenceTypeHandler<?> jacksonListReferenceTypeHandler(){
        return new JacksonListReferenceTypeHandler<>();
    }

    @Bean
    public JacksonTypeHandler<?> jacksonTypeHandler(){
        return new JacksonTypeHandler<>();
    }
}
