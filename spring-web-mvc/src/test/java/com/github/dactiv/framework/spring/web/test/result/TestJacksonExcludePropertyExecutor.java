package com.github.dactiv.framework.spring.web.test.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.web.result.filter.FilterResultAnnotationBuilder;
import com.github.dactiv.framework.spring.web.result.filter.FilterResultSerializerProvider;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import com.github.dactiv.framework.spring.web.test.result.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestJacksonExcludePropertyExecutor {

    @SuppressWarnings("unchecked")
    @Test
    public void testFilter() {

        User user = new User();

        user.generateRole(5);

        List<String> basePackages = Collections.singletonList("com.github.dactiv.framework.spring.web.test.result.entity");

        FilterResultAnnotationBuilder builder = new FilterResultAnnotationBuilder(basePackages);

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setSerializerProvider(new FilterResultSerializerProvider());

        objectMapper.setFilterProvider(builder.getFilterProvider(objectMapper.getSerializationConfig()));

        objectMapper.setAnnotationIntrospector(builder);

        Casts.setObjectMapper(objectMapper);

        FilterResultHolder.set("unity");

        Map<String, Object> userMap = Casts.convertValue(user, Map.class);

        Assertions.assertEquals(userMap.size(), 3);

        Map<String, Object> userDetailMap = Casts.cast(userMap.get("userDetail"));

        Assertions.assertEquals(userDetailMap.size(), 4);

        List<Map<String, Object>> rolesList = Casts.cast(userMap.get("roles"));

        rolesList.forEach(r -> Assertions.assertEquals(r.size(), 4));

        // -----------------------------

        FilterResultHolder.clear();

        userMap = Casts.convertValue(user, Map.class);

        Assertions.assertEquals(userMap.size(), 8);

        userDetailMap = Casts.cast(userMap.get("userDetail"));

        Assertions.assertEquals(userDetailMap.size(), 5);

        rolesList = Casts.cast(userMap.get("roles"));

        rolesList.forEach(r -> Assertions.assertEquals(r.size(), 5));

    }
}
