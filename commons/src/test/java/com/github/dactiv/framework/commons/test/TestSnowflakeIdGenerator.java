package com.github.dactiv.framework.commons.test;

import com.github.dactiv.framework.commons.generator.twitter.SnowflakeIdGenerator;
import com.github.dactiv.framework.commons.generator.twitter.SnowflakeProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 测试雪环 id 生成器
 *
 * @author maurice.chen
 */
public class TestSnowflakeIdGenerator {

    @Test
    public void testGenerateId() {

        SnowflakeProperties snowflakeProperties = new SnowflakeProperties();
        snowflakeProperties.setServiceId("001");
        snowflakeProperties.setWorkerId(1);
        snowflakeProperties.setDataCenterId(1);

        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(snowflakeProperties);

        Assertions.assertEquals(snowflakeIdGenerator.generateId().length(), 32);
    }
}
