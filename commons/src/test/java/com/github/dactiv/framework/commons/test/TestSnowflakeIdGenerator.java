package com.github.dactiv.framework.commons.test;

import com.github.dactiv.framework.commons.generator.twitter.SnowflakeIdGenerator;
import com.github.dactiv.framework.commons.generator.twitter.SnowflakeProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * 测试雪环 id 生成器
 *
 * @author maurice.chen
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class TestSnowflakeIdGenerator {

    @Test
    public void testGenerateId() {

        SnowflakeProperties snowflakeProperties = new SnowflakeProperties();
        snowflakeProperties.setServiceId("001");
        snowflakeProperties.setWorkerId(1);
        snowflakeProperties.setDataCenterId(1);

        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(snowflakeProperties);

        Assert.assertEquals(snowflakeIdGenerator.generateId().length(), 32);

    }
}
