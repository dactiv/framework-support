package com.github.dactiv.framework.mybatis.plus.test;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.mybatis.plus.test.entity.AllTypeEntity;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

@SpringBootTest
public class TestQueryGenerator {

    @Autowired
    private MybatisPlusQueryGenerator<AllTypeEntity> queryGenerator;

    @Test
    public void testAll() {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        map.add("filter_[id_eqn]", true);
        map.add("filter_[id_eq]", BigDecimal.ONE.intValue());

        map.add("filter_[user_id_gte]", BigDecimal.ZERO.intValue());
        map.add("filter_[user_id_lte]", Integer.MIN_VALUE);

        map.add("filter_[retry_count_gt]", BigDecimal.ZERO.intValue());
        map.add("filter_[retry_count_lt]", Integer.MIN_VALUE);

        map.put("filter_[sync_status_in]", Arrays.asList(BigDecimal.ZERO.intValue(),BigDecimal.ONE.intValue()));
        map.put("filter_[sync_status_nin]", Arrays.asList(Integer.MIN_VALUE, Integer.MIN_VALUE));

        map.add("filter_[type_ne]", BigDecimal.ZERO.intValue());
        map.add("filter_[type_nen]", true);

        map.add("filter_[remark_like]", "abc");
        map.add("filter_[remark_llike]", "def");
        map.add("filter_[remark_rlike]", "ghi");

        map.put("filter_[creation_time_between]", Arrays.asList(new Date(), new Date()));

        map.add("filter_[city.id_jin]", "nanning");
        map.put("filter_[area_jin]", Arrays.asList("qingqiuqu", "xingningqu"));

        map.add("filter_[city.name_jeq]", "nanning");

        QueryWrapper<AllTypeEntity> queryWrapper = queryGenerator.createQueryWrapperFromMap(map);

        String targetSql = queryWrapper.getTargetSql();

        Assertions.assertTrue(targetSql.contains("id IS NULL"));
        Assertions.assertTrue(targetSql.contains("id = ?"));

        Assertions.assertTrue(targetSql.contains("user_id >= ?"));
        Assertions.assertTrue(targetSql.contains("user_id <= ?"));

        Assertions.assertTrue(targetSql.contains("retry_count > ?"));
        Assertions.assertTrue(targetSql.contains("retry_count < ?"));

        Assertions.assertTrue(targetSql.contains("sync_status IN (?,?)"));
        Assertions.assertTrue(targetSql.contains("sync_status NOT IN (?,?)"));

        Assertions.assertTrue(targetSql.contains("type IS NOT NULL"));
        Assertions.assertTrue(targetSql.contains("type <> ? "));

        Assertions.assertEquals(StringUtils.countMatches(targetSql,"remark LIKE ?"), 3);

        Assertions.assertTrue(targetSql.contains("creation_time BETWEEN ? AND ?"));

        Assertions.assertTrue(targetSql.contains("JSON_CONTAINS(city->'$[*].id', ?, '$')"));
        Assertions.assertTrue(targetSql.contains("city->'$.name' = ?"));
        Assertions.assertTrue(targetSql.contains("(JSON_CONTAINS(area, ?) OR JSON_CONTAINS(area, ?))"));

    }
}
