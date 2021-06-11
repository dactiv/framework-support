package com.github.dactiv.framework.spring.web.test.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.result.filter.FilterResultAnnotationBuilder;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import com.github.dactiv.framework.spring.web.test.result.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestJacksonExcludePropertyExecutor {

    @SuppressWarnings("unchecked")
    @Test
    public void testFilter() {

        User user = new User();

        user.generateRole(5);

        List<String> basePackages = Collections.singletonList("com.github.dactiv.framework.spring.web.test.result.entity");

        FilterResultAnnotationBuilder builder = new FilterResultAnnotationBuilder(basePackages);

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setFilterProvider(builder.getFilterProvider(objectMapper.getSerializationConfig()));

        objectMapper.setAnnotationIntrospector(builder);

        Casts.setObjectMapper(objectMapper);

        FilterResultHolder.set("unity");

        Map<String, Object> userMap = Casts.convertValue(user, Map.class);

        Assert.assertEquals(userMap.size(), 3);

        Map<String, Object> userDetailMap = Casts.cast(userMap.get("userDetail"));

        Assert.assertEquals(userDetailMap.size(), 4);

        List<Map<String, Object>> rolesList = Casts.cast(userMap.get("roles"));

        rolesList.forEach(r -> Assert.assertEquals(r.size(), 4));

    }
}
