package com.github.dactiv.framework.spring.web.filter.generator.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.spring.web.filter.QueryGenerator;
import com.github.dactiv.framework.spring.web.filter.condition.Condition;
import com.github.dactiv.framework.spring.web.filter.condition.ConditionParser;
import com.github.dactiv.framework.spring.web.filter.condition.ConditionType;
import com.github.dactiv.framework.spring.web.filter.condition.support.SimpleConditionParser;
import com.github.dactiv.framework.spring.web.filter.generator.WildcardParser;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.wildcard.EqWildcardParser;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.wildcard.LikeWildcardParser;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.wildcard.NeWildcardParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mybatis-Plus 查询生成器实现
 *
 * @author maurice.chen
 */
public class MybatisPlusQueryGenerator<T> implements QueryGenerator<QueryWrapper<T>> {

    /**
     * 通配符解析器集合
     */
    private final List<WildcardParser> wildcardParsers;

    /**
     * 条件解析器集合
     */
    private final List<ConditionParser> conditionParsers;

    /**
     * 创建一个 Mybatis-Plus 查询生成器实现
     */
    public MybatisPlusQueryGenerator() {
        this.wildcardParsers = getDefaultWildcardParserList();
        this.conditionParsers = getDefaultConditionParserList();
    }

    /**
     * 创建一个 Mybatis-Plus 查询生成器实现
     *
     * @param wildcardParsers 通配符解析器集合
     * @param conditionParsers 条件解析器集合
     */
    public MybatisPlusQueryGenerator(List<WildcardParser> wildcardParsers, List<ConditionParser> conditionParsers) {
        this.wildcardParsers = wildcardParsers;
        this.conditionParsers = conditionParsers;
    }

    @Override
    public QueryWrapper<T> generate(List<Condition> conditions) {

        QueryWrapper<T> queryWrapper = Wrappers.query();

        for (Condition c : conditions) {

            QueryWrapper<T> temp = queryWrapper;

            if (ConditionType.Or.equals(c.getType())) {
                temp = queryWrapper.or();
            }

            List<WildcardParser> result = getWildcardParserList()
                    .stream()
                    .filter(w -> w.isSupport(c.getName()))
                    .collect(Collectors.toList());

            for (WildcardParser wildcardParser : result) {
                wildcardParser.structure(c.getProperty(), temp);
            }

        }

        return queryWrapper;
    }

    @Override
    public List<WildcardParser> getWildcardParserList() {
        return wildcardParsers;
    }

    @Override
    public List<ConditionParser> getConditionParserList() {
        return conditionParsers;
    }

    /**
     * 获取默认通配符解析器集合
     *
     * @return 通配符解析器集合
     */
    public List<WildcardParser> getDefaultWildcardParserList() {
        return Arrays.asList(
                new EqWildcardParser(),
                new NeWildcardParser(),
                new LikeWildcardParser()
        );
    }

    /**
     * 获取条件解析器集合
     *
     * @return 条件解析器集合
     */
    public List<ConditionParser> getDefaultConditionParserList() {
        return Collections.singletonList(new SimpleConditionParser());
    }
}
