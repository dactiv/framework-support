package com.github.dactiv.framework.mybatis;


import com.github.dactiv.framework.mybatis.interceptor.json.support.JacksonJsonCollectionPostInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis 自动配置实现
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnProperty(prefix = "dactiv.mybatis", value = "enabled", matchIfMissing = true)
public class MybatisAutoConfiguration {

    @Bean
    JacksonJsonCollectionPostInterceptor jacksonCollectionPostInterceptor(){
        return new JacksonJsonCollectionPostInterceptor();
    }
}
