package com.github.dactiv.framework.spring.web.test.result;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import com.github.dactiv.framework.spring.web.test.result.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestJacksonExcludePropertyExecutor {

    @SuppressWarnings("unchecked")
    @Test
    public void testFilter() {

        User user = new User();

        user.generateRole(5);

        FilterResultHolder.add("unity");

        Map<String, Object> userMap = Casts.convertValue(user, Map.class);

        Assertions.assertEquals(userMap.size(), 3);

        Map<String, Object> userDetailMap = Casts.cast(userMap.get("userDetail"));

        Assertions.assertEquals(userDetailMap.size(), 4);

        List<Map<String, Object>> rolesList = Casts.cast(userMap.get("roles"));

        rolesList.forEach(r -> Assertions.assertEquals(r.size(), 4));

        // ----------------------------- //

        FilterResultHolder.clear();

        userMap = Casts.convertValue(user, Map.class);

        Assertions.assertEquals(userMap.size(), 8);

        userDetailMap = Casts.cast(userMap.get("userDetail"));

        Assertions.assertEquals(userDetailMap.size(), 5);

        rolesList = Casts.cast(userMap.get("roles"));

        rolesList.forEach(r -> Assertions.assertEquals(r.size(), 5));

        // ----------------------------- //

    }
}
