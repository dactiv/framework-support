package com.github.dactiv.framework.spring.web.test.result;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.result.filter.executor.JacksonFilterPropertyExecutor;
import com.github.dactiv.framework.spring.web.test.result.entity.TestUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestJacksonFilterPropertyExecutor {

    private final JacksonFilterPropertyExecutor filterPropertyExecutor = new JacksonFilterPropertyExecutor();

    @SuppressWarnings("unchecked")
    @Test
    public void testFilter() {

        TestUser testUser = new TestUser();

        testUser.setId(1);
        testUser.setAge(31);
        testUser.setNickname("maurice.chen");
        testUser.setUsername("1877892****");
        testUser.setSex("1");
        testUser.setCreationTime((int)new Date().getTime());

        TestUser testUser2 = new TestUser();

        testUser2.setId(2);
        testUser2.setAge(31);
        testUser2.setNickname("maurice.chen");
        testUser2.setUsername("1877892****");
        testUser2.setSex("1");
        testUser2.setCreationTime((int)new Date().getTime());

        testUser.setNoFilterPropertiesUser(testUser2);
        testUser.setFilterPropertiesUser(testUser2);
        testUser.setClassTypeFilterPropertiesUser(testUser2);

        TestUser testUser3 = new TestUser();

        testUser3.setId(2);
        testUser3.setAge(31);
        testUser3.setNickname("maurice.chen");
        testUser3.setUsername("1877892****");
        testUser3.setSex("1");
        testUser3.setCreationTime((int)new Date().getTime());

        TestUser testUser4 = new TestUser();

        testUser4.setId(2);
        testUser4.setAge(31);
        testUser4.setNickname("maurice.chen");
        testUser4.setUsername("1877892****");
        testUser4.setSex("1");
        testUser4.setCreationTime((int)new Date().getTime());

        testUser.getUserList().add(testUser3);
        testUser.getUserList().add(testUser4);

        Object result = filterPropertyExecutor.filter("unity", testUser);

        Map<String, Object> map = Casts.convertValue(result, Map.class);

        Arrays.stream(TestUser.DEFAULT_FILTER_PROPERTIES).forEach(s -> Assert.assertFalse(map.containsKey(s)));

        Map<String, Object> noFilterPropertiesUser = Casts.convertValue(map.get("noFilterPropertiesUser"), Map.class);

        Assert.assertEquals(noFilterPropertiesUser.size(),10);

        Map<String, Object> filterPropertiesUser = Casts.convertValue(map.get("filterPropertiesUser"), Map.class);

        Assert.assertEquals(filterPropertiesUser.size(),7);

        Map<String, Object> classTypeFilterPropertiesUser = Casts.convertValue(map.get("classTypeFilterPropertiesUser"), Map.class);

        Assert.assertEquals(classTypeFilterPropertiesUser.size(),6);

        List<Map<String, Object>> userList = Casts.convertValue(map.get("userList"), List.class);

        userList.forEach(m -> Assert.assertEquals(m.size(),7));
    }
}
