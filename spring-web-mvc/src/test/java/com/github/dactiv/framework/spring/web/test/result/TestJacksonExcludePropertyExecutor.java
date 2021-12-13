package com.github.dactiv.framework.spring.web.test.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import com.github.dactiv.framework.spring.web.test.result.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestJacksonExcludePropertyExecutor {

    @Autowired
    private ObjectMapper objectMapper;

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

        User temp = Casts.readValue("{\"id\":1,\"creationTime\":1,\"nickname\":\"咏春.叶问\",\"username\":\"maurice.chen\",\"sex\":\"male\",\"age\":27,\"userDetail\":{\"id\":1,\"creationTime\":\"2021-12-13T02:16:43.436+00:00\",\"ip\":\"127.0.0.1\",\"status\":\"0\",\"uuid\":\"361d36b9-6d3f-457e-98c0-eb03644ed103\"},\"roles\":[{\"id\":1,\"creationTime\":\"2021-12-13T02:16:43.436+00:00\",\"name\":\"maurice.chen\",\"authority\":\"admin\",\"uuid\":\"ce38fd33-3e34-415b-8597-809ebaf1da34\"},{\"id\":1,\"creationTime\":\"2021-12-13T02:16:43.436+00:00\",\"name\":\"maurice.chen\",\"authority\":\"admin\",\"uuid\":\"b622fce9-9e38-49e3-8ca6-b63a58997351\"},{\"id\":1,\"creationTime\":\"2021-12-13T02:16:43.436+00:00\",\"name\":\"maurice.chen\",\"authority\":\"admin\",\"uuid\":\"6be46d61-80c5-4b37-b051-0b16f7bcdeac\"},{\"id\":1,\"creationTime\":\"2021-12-13T02:16:43.436+00:00\",\"name\":\"maurice.chen\",\"authority\":\"admin\",\"uuid\":\"aab14e6f-3fc4-4da2-8d2c-b957cc37e7d4\"},{\"id\":1,\"creationTime\":\"2021-12-13T02:16:43.436+00:00\",\"name\":\"maurice.chen\",\"authority\":\"admin\",\"uuid\":\"6884ef4a-a815-4c8b-91ad-3f612f7e0001\"}]}", User.class);

    }
}
